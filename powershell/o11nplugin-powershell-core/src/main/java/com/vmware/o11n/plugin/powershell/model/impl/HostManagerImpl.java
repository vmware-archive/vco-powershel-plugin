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

package com.vmware.o11n.plugin.powershell.model.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.vmware.o11n.plugin.powershell.config.ConfigurationChangeListener;
import com.vmware.o11n.plugin.powershell.config.ConfigurationService;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.powershell.model.Host;
import com.vmware.o11n.plugin.powershell.model.HostManager;

@Component
public class HostManagerImpl implements HostManager, InitializingBean, ConfigurationChangeListener{

	@Autowired
	private ConfigurationService configurationService;
	
    @Autowired
    private ApplicationContext applicationContext;

	private final Map<String, Host> hosts;

	private boolean load = false;

	// Initialize hash
	private void load() {
		if ( load  == false ){
			Collection<PowerShellHostConfig> cfgHosts = configurationService.findAll();
			for (PowerShellHostConfig config : cfgHosts){
			    Host live = applicationContext.getBean(Host.class);
	            live.init(config);
	            hosts.put(config.getId(), live);
			}
		}
		load = true;
	}

	public HostManagerImpl (){
		hosts = new ConcurrentHashMap<String, Host>();	
	}
	
	@Override
	public Collection<Host> findAll() {
		load();
		
		return hosts.values();
	}



	@Override
	public Host findById(String id) {
		load();
		
		return hosts.get(id);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		configurationService.addConfigurationChangeListener(this);
	}

	@Override
	public void onHostUpdated(PowerShellHostConfig config) {
        Host live = hosts.get(config.getId());
        if (live != null) {
            live.init(config);
        } else {
            // connection just added, create it
            live = applicationContext.getBean(Host.class);
            live.init(config);
            hosts.put(config.getId(), live);
        }
	}

	@Override
	public void onHostRemoved(PowerShellHostConfig config) {
        Host host = hosts.remove(config.getId());
        if (host != null) {
        	host.destroy();
        }
	}
}
