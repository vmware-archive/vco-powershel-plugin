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
package com.vmware.o11n.plugin.powershell.scripting;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vmware.o11n.plugin.PowerShellPluginFactory;
import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.config.AuthorizationMode;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.powershell.model.CmdletInfo;
import com.vmware.o11n.plugin.powershell.model.Host;
import com.vmware.o11n.plugin.powershell.model.InvocationResult;
import com.vmware.o11n.plugin.powershell.model.Session;
import com.vmware.o11n.plugin.powershell.model.SnapInInfo;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.PluginFactoryAware;

/**
 * Represents a remote Machine with installed PowerShell on it.
 */
@VsoObject(create = false, description="Represents a remote host with PowerShell installed on it.")
@VsoFinder(name = Constants.FINDER_POWER_SHELL_HOST, idAccessor = "getId()", image = "images/host-32x32.PNG",
        relations = { @VsoRelation(inventoryChildren = true, name = Constants.RELATION_HOST_HAS_SNAPIN_ROOT,
                type = Constants.FINDER_POWER_SHELL_SNAPIN_ROOT) })
@Component
public class PowerShellHost implements PluginFactoryAware {

    private Host host;
    private AbstractSpringPluginFactory factory;

    public PowerShellHost() {
    }

    public void init(Host host) {
        this.host = host;
    }

    public Host getHost() {
        return this.host;
    }

    @VsoProperty(readOnly = true, description="The unique ID of this object")
    public String getId() {
        return host.getHostConfig().getId();
    }

    @VsoProperty(readOnly = true, description="Logical name given to the remote PowerShell host")
    public String getName() {
        return host.getHostConfig().getName();
    }

    @VsoProperty(readOnly = true, description="The type of the communication protocol i.e. WinRM or SSH")
    public String getType() {
        return host.getHostConfig().getType();
    }

    @VsoProperty(readOnly = true, description="The IP/Hostname of the remote host")
    public String getConnectionURL() {
        return host.getHostConfig().getConnectionURL();
    }

    @VsoProperty(readOnly = true, 
                    description="The transport protocol in case of WinRM type of communication. Can be either HTTPS or HTTP")
    public String getTransportProtocol() {
        return host.getHostConfig().getTransportProtocol();
    }
    
    @VsoProperty(readOnly = true, description="The port on which to connect in case of WinRM type of communication.")
    public String getPort() {
        return host.getHostConfig().getPort();
    }
    
    @VsoProperty(readOnly = true, description="The username of the user that logs to the remote PowerShell Machine")
    public String getUsername() {
        return getUserToken().getUserName();
    }

    @VsoMethod(description="Returns the PowerShellHostConfig instance for this PowerShellHost")
    public PowerShellHostConfig getHostConfig() {
        return host.getHostConfig();
    }

    @VsoMethod(description="Opens a new PowerShellSession, which can be used to run PowerShell scrits and cmdlets")
    public PowerShellSession openSession() {
        Session session = null;
        session = host.openSession(getUserToken());
        if (session != null) {
            return createPowerShellSession(session);
        }

        return null;
    }

    @VsoMethod(description="Opens a new PowerShellSession with provided credentials, which can be used to run PowerShell scrits and cmdlets")
    public PowerShellSession openSessionAs(String name, String password) {
        Session session = null;
        session = host.openSession(new PowerShellUserTokenImpl(name, password.toCharArray()));
        if (session != null) {
            return createPowerShellSession(session);
        }

        return null;
    }

    private PowerShellUserTokenImpl getUserToken() {
        String username = null;
        char[] password = null;
        if (host.getHostConfig().getAuthorizationMode() == AuthorizationMode.Shared) {
            username = host.getHostConfig().getUsername();
            password = host.getHostConfig().getPassword().toCharArray();
        } else if (host.getHostConfig().getAuthorizationMode() == AuthorizationMode.PerUser) {
            username = factory.getUserToken().getUserName();
            password = factory.getUserToken().getPassword();
        } else {
            throw new IllegalArgumentException("Invalid autorization mode.");
        }

        return new PowerShellUserTokenImpl(username, password);
    }

    @VsoMethod(description="Closes the session with the given sessionId")
    public void closeSession(String sessionId) {
        host.closeSession(sessionId);
    }

    @VsoMethod(description= "Invokes powershell script without adding it to pipeline.")
    public InvocationResult invokeScript(String script) {
        PowerShellSession session = openSession();
        InvocationResult result = session.invokeScript(script);
        closeSession(session.getSessionId());
        return result;
    }

    @VsoMethod(description="Returns Session for the given sessionId." +
    		                " Exception is thrown if session does not exist or has been closed.")
    public PowerShellSession getSession(String sessionId) {
        Session session = host.getSession(sessionId);
        return createPowerShellSession(session);
    }

    @VsoMethod(description="Returns the inPipeline flag of the session identified by sessionId")
    public boolean inPipeline(String sessionId) {
        PowerShellSession session = getSession(sessionId);
        return session.inPipeline();
    }

    @VsoMethod(description="Starts a pipeline in the context of the session identified by sessionId")
    public void startPipeline(String sessionId) {
        PowerShellSession session = getSession(sessionId);
        session.startPipeline();
    }

    @VsoMethod(description="Executes the pipeline associated with the session with id==sessionId" +
    		                " and clears the pipeline, removing all commands from it.")
    public void endPipeline(String sessionId) {
        PowerShellSession session = getSession(sessionId);
        session.endPipeline();
    }

    //TODO : Add caching
    public PowerShellSnapInRoot getSnapInRoot() {
        return new PowerShellSnapInRoot(this);
    }

    @Override
    public void setPluginFactory(AbstractSpringPluginFactory factory) {
        this.factory = factory;

    }

    @Override
    public AbstractSpringPluginFactory getPluginFactory() {
        // TODO Auto-generated method stub
        return factory;
    }

    public CmdletInfo getCmdletInfo(String cmdletName, String snapinName) {
        
        return host.getCmdletInfo(cmdletName, snapinName, getUserToken());
    }

    public List<CmdletInfo> getCmdletInfo(String snapinName) {
        
        return host.getCmdletInfo(snapinName, getUserToken());
    }

    public List<SnapInInfo> getSnapIns() {
        
        return host.getSnapIns(getUserToken());
    }

    public SnapInInfo getSnapIn(String name) {

        return host.getSnapIn(name, getUserToken());
    }
    
    private PowerShellSession createPowerShellSession(Session session){
        return ((PowerShellPluginFactory)factory).createPowerShellSession(session, host);
    }

}
