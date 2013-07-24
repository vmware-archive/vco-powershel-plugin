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

package com.vmware.o11n.plugin.powershell.config.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.dunes.model.client.IVSOFactoryClient;
import ch.dunes.util.DunesServerException;

import com.vmware.o11n.plugin.powershell.config.ConfigurationChangeListener;
import com.vmware.o11n.plugin.powershell.config.ConfigurationService;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.sdk.spring.CurrentFactory;

@Component
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	private final Collection<ConfigurationChangeListener> listeners;
	
	private Map<String, PowerShellHostConfig> hostsById;
	
    private boolean loaded;

    @Autowired
	private CurrentFactory current;

	public ConfigurationServiceImpl() {
		listeners = new CopyOnWriteArrayList<ConfigurationChangeListener>();
	}

	@Override
	public void addConfigurationChangeListener(ConfigurationChangeListener li) {
		listeners.add(li);
	}

    private void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        hostsById = new ConcurrentHashMap<String, PowerShellHostConfig>();

        reload();
    }

    @Override
    public synchronized void reload() {
        try {
            HostConfigPersister persister = createPersister();
            List<PowerShellHostConfig> all = persister.getAll();
            for (PowerShellHostConfig hostConfig : all) {
                PowerShellHostConfig prev = hostsById.get(hostConfig.getId());
                if (prev == null || !hostConfig.equals(prev)) {
                    hostsById.put(hostConfig.getId(), hostConfig);
                    fireUpdate(hostConfig);
                }
            }

            //build difference between old available configs and new
            Set<String> old = new HashSet<String>(hostsById.keySet());
            for (PowerShellHostConfig config : all) {
                old.remove(config.getId());
            }

            //delete all configs that ware present before but are missing now
            for (String id : old) {
                remove(id);
            }
        } catch (RemoteException e) {
            log.warn("", e);
        } catch (LoginException e) {
            log.warn("", e);
        } catch (DunesServerException e) {
            log.warn("", e);
        }
    }

    @Override
	public PowerShellHostConfig findConfigById(String id) {
    	load();
    	
		return hostsById.get(id);
	}

	@Override
	public Collection<PowerShellHostConfig> findAll() {
        load();

        return hostsById.values();
	}

    @Override
    public synchronized PowerShellHostConfig remove(String id) {
        load();

        final PowerShellHostConfig removed = hostsById.remove(id);
        if (removed != null) {
            fireRemoved(removed);

            try {
                HostConfigPersister persister = createPersister();
                persister.deleteConfig(removed.getId().toString());
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        }

        return removed;
    }

	@Override
	public synchronized void update(PowerShellHostConfig config) throws IOException {
        load();
		
		try {
            HostConfigPersister persister = createPersister();
            
		    if (!StringUtils.isEmpty(config.getId())){
		        //Update existing configuration
		        if (persister.getResourceId(config.getId()) == null){
		            // Configuration with specified ID can not be found.  
		            throw  new RuntimeException("Invalid configuration.");
		        }
		    }  else {
		        //New configuration
		        config.setId(UUID.randomUUID().toString());
		    }
		    
			//opitmization
			persister.updateConfig(config.getId(), config);
			// update cache 
			hostsById.put(config.getId(), config);
			fireUpdate(config);
		} catch (RemoteException e) {
			throw new IOException(e);
		} catch (LoginException e) {
			throw new IOException(e);
		} catch (DunesServerException e) {
			throw new IOException(e);
		}
	}

	private void fireUpdate(PowerShellHostConfig config) {
		for (ConfigurationChangeListener li : listeners) {
			try {
				li.onHostUpdated(config);
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}
	
	private void fireRemoved(PowerShellHostConfig config) {
		for (ConfigurationChangeListener li : listeners) {
			try {
				li.onHostRemoved(config);
			} catch (Exception e) {
				//log
			}
		}
	}
		
    private HostConfigPersister createPersister() throws RemoteException, LoginException, DunesServerException {
        IVSOFactoryClient vsoFactoryClient = current.get().getVsoFactoryClient();
        vsoFactoryClient.clearCache();
        return new HostConfigPersister(vsoFactoryClient);
    }

	
}
