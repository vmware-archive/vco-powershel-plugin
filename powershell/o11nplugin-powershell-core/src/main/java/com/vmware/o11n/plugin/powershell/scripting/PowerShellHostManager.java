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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import ch.dunes.vso.sdk.api.IPluginFactory;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.config.AuthorizationMode;
import com.vmware.o11n.plugin.powershell.config.AutorizationMode;
import com.vmware.o11n.plugin.powershell.config.ConfigurationService;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.powershell.model.Host;
import com.vmware.o11n.plugin.powershell.model.InvocationResult;
import com.vmware.o11n.plugin.powershell.model.PowerShellException;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.PluginFactoryAware;
import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler;

@VsoObject(create = false, singleton = true, description="Manager for configuring the PowerShell plug-in hosts")
@Component
public class PowerShellHostManager implements PluginFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(PowerShellHostManager.class);
    
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private GlobalPluginNotificationHandler notificationHandler;

    private AbstractSpringPluginFactory factory;

    public static PowerShellHostManager createScriptingSingleton(IPluginFactory factory) {
        return ((com.vmware.o11n.plugin.PowerShellPluginFactory) factory)
                .createScriptingObject(PowerShellHostManager.class);
    }

    @VsoMethod(description="Creates a PowerShellHost for the passed in as argument PowerShellHostConfig if it doesn't already exist." +
    		               " In case it already exists the PowerShellHost is updated.")
    public PowerShellHost update(PowerShellHostConfig hostConfig) throws IOException, ValidationException {
        validatePowerShellHost(hostConfig);
        configurationService.update(hostConfig);
        notificationHandler.notifyElementsInvalidate();
        PowerShellHost host = (PowerShellHost) factory.find(Constants.FINDER_POWER_SHELL_HOST, hostConfig.getId());
        return host;
    }

    @VsoMethod(description="Removes the PowerShellHostConfig with the passed in as argument id from the configuration of the plugin. ")
    public void remove(String id) throws IOException {
        PowerShellHostConfig cfg = configurationService.remove(id);
        if (cfg != null ){
            notificationHandler.notifyElementsInvalidate();
        } else {
            throw new PowerShellException("Invalid host.");  
        }
    }

    @VsoMethod(description="Checks if the provided hostConfig parameter contains settings that the plugin can use to successfully connect to a remote PowerShell machine." +
    		                " In case the configurations are not valid the method throws appropriate exception.")
    public void validatePowerShellHost(PowerShellHostConfig hostConfig) throws ValidationException{
        if(hostConfig == null){
            throw new ValidationException("Provided null parameter to validate.");
        }

        validateProperties(hostConfig);
        
        validateConnectivityAndFunctionallity(hostConfig);
    }

    private void validateProperties(PowerShellHostConfig hostConfig) throws ValidationException {
        if(hostConfig.getName() == null){
            throw new ValidationException("Validation error: " + "name for the host is null");
        }
        
        if(hostConfig.getType()== null){
            throw new ValidationException("Validation error: " + "type for the host is null");
        } else if(PowerShellHostConfig.POWERSHELL_HOST_WINRM.equals(hostConfig.getType()) 
                        && hostConfig.getTransportProtocol() == null){
            throw new ValidationException("Validation error: " + "Transport protocol for WinRM is null");
        }
        
        if(hostConfig.getType()== null){
            throw new ValidationException("Validation error: " + "type for the host is null");
        }        
        
        if(hostConfig.getConnectionURL() == null){
            throw new ValidationException("Validation error: " + "connection URL is null");
        }
        
        if (hostConfig.getAuthorizationMode() == AuthorizationMode.Shared) {//no check for credentials for the AutorizationMode.PerUser
            if(hostConfig.getUsername() == null){
                throw new ValidationException("Validation error: " + "username is null"); 
            }
            
            if(hostConfig.getPassword() == null){
                throw new ValidationException("Validation error: " + "password is null");
            }    
        }
    }
    
    private void validateConnectivityAndFunctionallity(PowerShellHostConfig hostConfig) throws ValidationException {
        Host h = null;
        if(applicationContext != null){
            h = applicationContext.getBean(Host.class);
        } else {
            //should be here only for JUnit tests
            log.warn("New instances of Host.class should'n be created without Spring, because of caching.");
            log.warn("Ignore this if logged from tests.");
            h = new Host();
        }
        
        InvocationResult res = null;
        try {
            h.init(hostConfig);
            h.setCacheEnabled(false);
            res = h.invokeCommand("$host", getUserToken(hostConfig));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String msg = e.getMessage();
            if (StringUtils.isEmpty(msg)){
                msg = "Connection with PowerShell server can not be validate. Check server log for more details.";
            }
            throw new ValidationException(msg, e);        
        } finally {
            h.destroy();
        }
        
        if(res == null){
            throw new ValidationException("Connection with PowerShell server can not be validate. Check server log for more details.");
        }
    }

    private PowerShellUserTokenImpl getUserToken(PowerShellHostConfig hostConfig) {
        String username = null;
        char[] password = null;
        if (hostConfig.getAuthorizationMode() == AuthorizationMode.Shared) {
            username = hostConfig.getUsername();
            password = hostConfig.getPassword().toCharArray();
        } else if (hostConfig.getAuthorizationMode() == AuthorizationMode.PerUser) {
            username = factory.getUserToken().getUserName();
            password = factory.getUserToken().getPassword();
        } else {
            throw new IllegalArgumentException("Invalid autorization mode.");
        }

        return new PowerShellUserTokenImpl(username, password);
    }    
    
    @Override
    public AbstractSpringPluginFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setPluginFactory(AbstractSpringPluginFactory factory) {
        this.factory = factory;
    }
}
