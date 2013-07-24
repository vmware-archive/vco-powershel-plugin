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

package com.vmware.o11n.plugin.powershell;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.vso.sdk.helper.SDKHelper;

import com.vmware.o11n.plugin.PowerShellPluginAdaptor;

public class PowerShellPluginConfiguration {
    
    private static PowerShellPluginConfiguration instance = null;
    private static final Logger log = LoggerFactory.getLogger(PowerShellPluginConfiguration.class);

    private Properties pluginConfig = new Properties();
    
    private PowerShellPluginConfiguration(){
        try {
            String pluginConfigPath = SDKHelper.getConfigurationPathForPluginName(PowerShellPluginAdaptor.PLUGIN_NAME);
            pluginConfig = SDKHelper.loadPropertiesForPluginName(PowerShellPluginAdaptor.PLUGIN_NAME);
            log.info("Plugin configuration taken from:" + pluginConfigPath);
        } catch (Exception e1) {
            log.info("Plugin configuration file not available. Using default values.");
        }        
    }
    
    public static PowerShellPluginConfiguration getInstance(){
        if(instance == null){
            instance = new PowerShellPluginConfiguration();
        }
        return instance;
    }

    long getDataCacheTimeToIdleSeconds() {
        long result = 180;
        try {
            result = new Long(pluginConfig.getProperty("invocation.result.expirationTimeInSecond", "180"));
        } catch (NumberFormatException e) {
            log.warn("Invalid value for invocation.result.expirationTimeInSecond", e);
        }
        return result;
    }

    int getInvocationResultMaxEntriesNumber() {
        int result = 1000;
        try {
            result = new Integer(pluginConfig.getProperty("invocation.result.maxEntriesNumber", "1000"));
        } catch (NumberFormatException e) {
            log.warn("Invalid value for invocation.result.maxEntriesNumber", e);
        }
        return result;
    }

    long getSessionCacheTimeToIdleSeconds() {
        long result = 7200;
        try {
            result = new Long(pluginConfig.getProperty("session.expirationTimeInSecond", "7200"));
        } catch (NumberFormatException e) {
            log.warn("Invalid value for session.expirationTimeInSecond", e);
        }
        return result;
    }

    int getSessionMaxNumber() {
        int result = 200;
        try {
            result = new Integer(pluginConfig.getProperty("session.maxNumber", "200"));
        } catch (NumberFormatException e) {
            log.warn("Invalid value for session.maxNumber", e);
        }
        return result;
    }

    
    //----------------------
    //  Authentication
    // ---------------------
    // == Kerberos ==
    /**
     *  Kerberos SPN template.
     *  Wherever {host} placeholder is used will be replaced with actual host name/ip. 
     */
    public String getKerberosSPNTemplate() {
        return pluginConfig.getProperty("auth.kerberos.spn.template", "HTTP/{host}");
    }
}
