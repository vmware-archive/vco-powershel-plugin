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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginAdaptor;


public final class PowerShellPluginAdaptor extends  AbstractSpringPluginAdaptor{

    private static final String DEFAULT_CONFIG = "com/vmware/o11n/plugin/pluginConfig.xml";
    
    public static final String PLUGIN_NAME = "PowerShell";
    
    public static final String ROOT = "Root";

    public static ClassLoader pluginClassLoader;//this should be DAR classloader
    
    @Override
    protected ApplicationContext createApplicationContext(ApplicationContext defaultParent) {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[] { DEFAULT_CONFIG }, false, defaultParent);

        pluginClassLoader = getClass().getClassLoader();//we expect spring
        applicationContext.setClassLoader(pluginClassLoader);
        applicationContext.refresh();
    
        return applicationContext;
    }
}