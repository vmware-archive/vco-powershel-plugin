/* 
 * Copyright (c) 2011-2012 VMware, Inc.
 *  
 * This file is part of the vCO PowerShell Plug-in.
 *  
 * The vCO PowerShell Plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation version 3 and no later version.
 *  
 * The vCO PowerShell Plug-in is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version 3
 * for more details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.vmware.o11n.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.springframework.beans.factory.annotation.Autowired;

import ch.dunes.vso.sdk.api.HasChildrenResult;
import ch.dunes.vso.sdk.api.QueryResult;

import com.vmware.o11n.plugin.powershell.CacheConstants;
import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.PluginCacheManager;
import com.vmware.o11n.plugin.powershell.config.ConfigurationService;
import com.vmware.o11n.plugin.powershell.model.BaseSession;
import com.vmware.o11n.plugin.powershell.model.CmdletInfo;
import com.vmware.o11n.plugin.powershell.model.Host;
import com.vmware.o11n.plugin.powershell.model.HostManager;
import com.vmware.o11n.plugin.powershell.model.Session;
import com.vmware.o11n.plugin.powershell.model.SnapInInfo;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellCmdlet;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellHost;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellSession;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellSnapIn;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellSnapInRoot;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.InventoryRef;
import com.vmware.o11n.plugin.sdk.spring.MultipartId;
import com.vmware.o11n.plugin.sdk.spring.PluginFactoryLifecycleAware;

public final class PowerShellPluginFactory extends AbstractSpringPluginFactory implements PluginFactoryLifecycleAware {

    @Autowired
    private HostManager hostManager;

    @Autowired
    private ConfigurationService configurationService;

    @Override
    public Object find(InventoryRef ref) {
        if (ref.isOfType(Constants.FINDER_POWER_SHELL_HOST)) {

            Host host = hostManager.findById(ref.getId());
            if (host != null) {
                return createPowerShellHost(host);
            } else {
                return null;
            }
        } else if (ref.isOfType(Constants.FINDER_POWER_SHELL_SNAPIN_ROOT)) {
            //PowerShellSnapinRoot id maches the host id
            Host host = hostManager.findById(ref.getId());
            if (host != null)  {
                PowerShellHost psHost = createPowerShellHost(host);
                return psHost.getSnapInRoot();
            } 
        } else if (ref.isOfType(Constants.FINDER_POWER_SHELL_SNAPIN)) {
            MultipartId snapinId = MultipartId.valueOf(ref.getId());
            MultipartId hostId = snapinId.getParent();
            Host host = hostManager.findById(hostId.toString());

            PowerShellHost psHost = createPowerShellHost(host);
            SnapInInfo snapIn = psHost.getSnapIn(snapinId.getLastPart());
            if (snapIn != null) {
                return new PowerShellSnapIn(snapIn, createPowerShellHost(host));
            }
        } else if (ref.isOfType(Constants.FINDER_POWER_SHELL_CMDLET)) {
            MultipartId cmdletId = MultipartId.valueOf(ref.getId());
            MultipartId snapinId = cmdletId.getParent();
            MultipartId hostId = snapinId.getParent();
            Host host = hostManager.findById(hostId.toString());

            PowerShellHost psHost = createPowerShellHost(host);
            CmdletInfo cmdlet = psHost.getCmdletInfo(cmdletId.getLastPart(), snapinId.getLastPart());
            if (cmdlet != null) {
                return new PowerShellCmdlet(cmdlet, createPowerShellHost(host));
            }
        } else if (ref.isOfType(Constants.FINDER_POWER_SHELL_REMOTE_PS_OBJECT)) {
            String[] split = ref.getId().split("@");
            String sessId = split[0];
            String objId = split[1];
            
            Object res = null; 
            res = getFromDataCache(objId, res);
            if (res == null ) {
                res = getFromSession(sessId, objId, res);
            }
            
            return res;
        } else {
            return null;
        }

        return null;
    }

    private Object getFromSession(String sessId, String objId, Object res) {
        Cache sessionCache = PluginCacheManager.getCache(CacheConstants.SESSION_CACHE);
        Element element = sessionCache.get(sessId);
        if(element != null){
            BaseSession sessionFromCache = (BaseSession)element.getObjectValue();
            if(sessionFromCache != null){
                res =  sessionFromCache.getResultById(objId);
            }
        }
        return res;
    }

    private Object getFromDataCache(String objId, Object res) {
//        BaseCacheWrapper<RemotePSObject> cachedInvocationResults = new InvokationResultsCache(CacheConstants.DATA_CACHE);
//        cachedInvocationResults.getFromCache(objId, sessId);
        
        Cache dataCache = PluginCacheManager.getCache(CacheConstants.DATA_CACHE);
        Element resultFromDataCache = dataCache.get(objId);
        if(resultFromDataCache != null){
            res = resultFromDataCache.getObjectValue();
        }
        return res;
    }

    @Override
    public QueryResult findAll(final String type, String query) {
        try {
            return doInCurrent(new Callable<QueryResult>() {
                @Override
                public QueryResult call() {
                    if (type.equals(Constants.FINDER_POWER_SHELL_HOST)) {
                        List<PowerShellHost> res = new ArrayList<PowerShellHost>();
                        for (Host host : hostManager.findAll()) {
                            res.add(createPowerShellHost(host));
                        }
                        return new QueryResult(res);
                    } else {
                        return null;
                    }
                
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    @Override
    public List<?> findChildrenInRootRelation(String type, String relationName) {
        List<PowerShellHost> hosts = new ArrayList<PowerShellHost>();
        for (Host host : hostManager.findAll()) {
            hosts.add(createPowerShellHost(host));
        }
        return hosts;
    }

    @Override
    public List<?> findChildrenInRelation(InventoryRef parent, String relationName) {
        if (parent.isOfType(Constants.FINDER_POWER_SHELL_HOST)) {
            Host host = hostManager.findById(parent.getId());
            PowerShellHost psHost = createPowerShellHost(host);
            List<PowerShellSnapInRoot> res = new ArrayList<PowerShellSnapInRoot>();
            if (relationName.equals(Constants.RELATION_HOST_HAS_SNAPIN_ROOT)) {
                res.add(psHost.getSnapInRoot());
            }
            return res;
        } else if (parent.isOfType(Constants.FINDER_POWER_SHELL_SNAPIN_ROOT)) {
            Host host = hostManager.findById(parent.getId());
            PowerShellHost psHost = createPowerShellHost(host);
            if (relationName.equals(Constants.RELATION_SNAPINROOT_HAS_SNAPIN)) {
                PowerShellSnapInRoot root = psHost.getSnapInRoot();
                return root.getSnapIns();
            }
        } else if (parent.isOfType(Constants.FINDER_POWER_SHELL_SNAPIN)) {
            MultipartId snapinId = MultipartId.valueOf(parent.getId());
            MultipartId hostId = snapinId.getParent();
            Host host = hostManager.findById(hostId.toString());
            if (relationName.equals(Constants.RELATION_SNAPIN_HAS_CMDLET)) {
                PowerShellHost psHost = createPowerShellHost(host);
                List<CmdletInfo> cmdletInfos = psHost.getCmdletInfo(snapinId.getLastPart());
                List<PowerShellCmdlet> cmdlets = new ArrayList<PowerShellCmdlet>();
                for (CmdletInfo info : cmdletInfos) {
                    cmdlets.add(new PowerShellCmdlet(info, psHost));
                }

                return cmdlets;
            }
        }

        return null;
    }

    @Override
    public HasChildrenResult hasChildrenInRootRelation(String type, String relationName) {

        return hostManager.findAll().isEmpty() ? HasChildrenResult.No : HasChildrenResult.Yes;
    }

    @Override
    public HasChildrenResult hasChildrenInRelation(InventoryRef ref, String relationName) {
        return HasChildrenResult.Unknown;
    }

    @Override
    public void invalidateAll() {
        try {
            doInCurrent(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    configurationService.reload();
                    invalidateUserData();
                    return null;
                }

                private void invalidateUserData() {
                    Collection<Host> hosts = hostManager.findAll();
                    for (Host host : hosts){
                        host.invalidate(getUserToken().getUserName());
                    }
                    
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void invalidate(String type, String id) {
        if (type.equals( Constants.FINDER_POWER_SHELL_HOST )) {
            Host host = hostManager.findById(id);
            host.invalidate(getUserToken().getUserName());
        } 
   }
    
    public PowerShellHost createPowerShellHost(Host host) {
        PowerShellHost psh = createScriptingObject(PowerShellHost.class);
        psh.init(host);
        return psh;
    }

    @Override
    public void beforeFactoryUninstall() {
        
    }

    public PowerShellSession createPowerShellSession(Session session, Host host){
        PowerShellSession powerShellSession = new PowerShellSession(session, host);
        
        return powerShellSession;
    }
}