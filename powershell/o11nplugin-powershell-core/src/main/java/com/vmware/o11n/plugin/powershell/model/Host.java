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

package com.vmware.o11n.plugin.powershell.model;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vmware.o11n.plugin.powershell.PluginCacheManager;
import com.vmware.o11n.plugin.powershell.config.AuthorizationMode;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.powershell.model.cache.SessionCache;
import com.vmware.o11n.plugin.powershell.model.impl.SshRemoteConnector;
import com.vmware.o11n.plugin.powershell.model.impl.winrm.WinRmRemoteConnector;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class Host {
    private static final Logger log = LoggerFactory.getLogger(Host.class);

	private PowerShellHostConfig hostConfig;
	
	private RemoteConnector impl;

	private SessionCache sessionsCache;

    private boolean cacheEnabled = true;

	public Host() {
		
	}

    public void init(final PowerShellHostConfig config) {
        log.debug("Initialize host :" + config.getName(), " , id:" + config.getId());

        PowerShellHostConfig newCfg = new PowerShellHostConfig(config);
        if (this.hostConfig == null){
            this.hostConfig = newCfg;
            sessionsCache = new SessionCache();
            initCache();
        } else if (!this.hostConfig.equals(config)) {
            this.hostConfig = newCfg;
            sessionsCache.invalidateAllSessions();
            invalidateCache();
        }
        
        initHostImpl();
            
    }

    private String getCacheName(){
        if (null != hostConfig){
            return hostConfig.getId();
        }
        return null;
    }

    private void invalidateCache() {
        if (cacheEnabled && ( null != getCacheName()) ){
            Cache cache = PluginCacheManager.getCache(getCacheName());
            if (null != cache){
                cache.removeAll();
            }
        }
    }

    private void initCache() {
        if ( cacheEnabled &&  ( null != getCacheName()) ){
            PluginCacheManager.getOrCreateCache(getCacheName());
        }
    }

    private boolean isCacheEnabled() {
        // TODO Auto-generated method stub
        return cacheEnabled ;
    }

	private void initHostImpl() {
		if (hostConfig.getType().equals(PowerShellHostConfig.POWERSHELL_HOST_SSH)) {
			impl = new SshRemoteConnector(hostConfig);
		} else if(hostConfig.getType().equals(PowerShellHostConfig.POWERSHELL_HOST_WINRM)){
		    impl = new WinRmRemoteConnector(hostConfig);
		}else {
			throw new IllegalArgumentException(hostConfig.getType()
					+ " is not supported. ");
		}
	}

	public PowerShellHostConfig getHostConfig() {
		return new PowerShellHostConfig(hostConfig);
	}

	public Session openSession(PowerShellUserToken token) {
		Session session = impl.openSession(token);
		sessionsCache.putInCache(session.getSessionId(), session);
		return session;
	}

	public void closeSession(String sessionId) {
	    Session sess = sessionsCache.getFromCache(sessionId);
	    if (sess != null){
	        sess.disconnect();
	        sessionsCache.removeFromCache(sess);
	    }
		
		//there is a CacheEventListener that will call disconnect() 
	}
	
    public Session getSession(String sessionId) {
		Session session = sessionsCache.getFromCache(sessionId);
		if (session == null ){
			throw new PowerShellException("Invalid session. SessionId:" + sessionId);
		} 
		
		return session;
	}

    public CmdletInfo getCmdletInfo(String cmdletName, String pssnapinName, PowerShellUserToken token) {
        UserProfileData userProfile = getOrCreateUserProfile(token.getUserName());
        assert(userProfile != null);
        
        CmdletInfo cmdletInfo = userProfile.getCmdlet(cmdletName, pssnapinName);
        if (cmdletInfo != null ){
            return cmdletInfo;
        }
        
        List<CmdletInfo> cmdletInfos = getCmdlets(cmdletName, pssnapinName, token);
        if (cmdletInfos.size()>0){
            cmdletInfo = cmdletInfos.get(0);
            userProfile.register(cmdletInfo);
        }

        return cmdletInfo;        
    }

    public List<CmdletInfo> getCmdletInfo(String pssnapinName, PowerShellUserToken token) {
        UserProfileData userProfile = getOrCreateUserProfile(token.getUserName());
        assert(userProfile != null);
        
        //Check if already loaded 
        // if no preload them 
        if ( userProfile.getCmdlets(pssnapinName) == null ) {
            List<CmdletInfo> cmdletInfo = getCmdlets(null, pssnapinName, token);
            userProfile.preloadCmdletsInfo(cmdletInfo, pssnapinName);
        }
    
        return userProfile.getCmdlets(pssnapinName);
    }
    
    private List<CmdletInfo> getCmdlets(String cmdletName, String pssnapinName, PowerShellUserToken token) {
        Session session = openSession(token);
        List<CmdletInfo> snapInInfos = CmdletInfoHelper.getCmdletInfo(session , cmdletName, pssnapinName);
        closeSession(session.getSessionId());
        return snapInInfos;
    }
	
    public List<SnapInInfo> getSnapIns(PowerShellUserToken token) {
        UserProfileData userProfile = getOrCreateUserProfile(token.getUserName());
        assert(userProfile != null);
        
        if ( userProfile.getSnapIns() == null ) {
            List<SnapInInfo> snapinInfo = getSnapInInfos(null, token);
            userProfile.preloadSnapinsInfo(snapinInfo);
        }
    
        return userProfile.getSnapIns();
    }
    
    public SnapInInfo getSnapIn(String name, PowerShellUserToken token) {
        UserProfileData userProfile = getOrCreateUserProfile(token.getUserName());
        assert(userProfile != null);
        
        SnapInInfo snapin = userProfile.getSnapIn(name);
        if (snapin != null ){
            return snapin;
        }
        
        List<SnapInInfo> snapInInfos = getSnapInInfos(name, token);
        if (snapInInfos.size()>0){
            snapin = snapInInfos.get(0);
            userProfile.register(snapin);
        }

        return snapin;
    }

    private UserProfileData getUserProfile(String userName) {
        return getFromCache(userName);
    }

    private UserProfileData getOrCreateUserProfile(String userName) {
        UserProfileData userProfile = getFromCache(userName);
        if (null == userProfile){
            userProfile = new UserProfileData(userName);
            putInCache(userName, userProfile);   
        }
        return userProfile;
    }
    
    private void putInCache(String userName, UserProfileData userProfile) {
        if (cacheEnabled ){
            Cache cache = getCache();
            if ( null != cache) {
                cache.put(new Element(userName, userProfile));
            }
        }
    }

    private UserProfileData getFromCache(String userName){
        if (cacheEnabled ){
            Cache cache = PluginCacheManager.getCache(getCacheName());
            if (null != cache) {
                Element element = cache.get(userName);
                if ( element != null){
                    return (UserProfileData) element.getObjectValue();
                }
            }
        }
        
        return null;
    }

    private List<SnapInInfo> getSnapInInfos(String name, PowerShellUserToken token){
        Session session = openSession(token);
        List<SnapInInfo> snapins = CmdletInfoHelper.getSnapin(session, name);
        closeSession(session.getSessionId());
        
        return snapins;
    }

    public InvocationResult invokeCommand(String script, PowerShellUserToken token) {
        Session session = null;
        InvocationResult res = null;;
        try {
        session = openSession(token);
        res = session.invoke(script);
        } finally {
            if (session != null) {
                closeSession(session.getSessionId());
            }
        }
        return res;
    }
    
	public boolean hasSessions() {
		return sessionsCache.hasValidSessions(); 
	}


    public void invalidate(String userName) {
        String key = userName; 
        if (hostConfig.getAuthorizationMode() == AuthorizationMode.Shared ) {
            key = hostConfig.getUsername();
        }
        
        if (null != getCache()) {
            getCache().remove(key);
        }
    }
    
    private Cache getCache() {
        if (isCacheEnabled() && (null != getCacheName())) {
          return PluginCacheManager.getOrCreateCache(getCacheName());
        }
        
        return null;
    }

    public void invalidate(String userName, String snapIn) {
        UserProfileData profile = getUserProfile(userName);
        if (profile != null ){
            profile.invalidateSnapin(snapIn);
        }
    }

    public void destroy() {
        if (cacheEnabled && ( null != getCacheName()) ){
            PluginCacheManager.removeCache(getCacheName());
        }
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
    
}
