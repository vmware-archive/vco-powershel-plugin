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

package com.vmware.o11n.plugin.powershell.model.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.vmware.o11n.plugin.powershell.PluginCacheManager;


public abstract class BaseCacheWrapper<E> {
    final protected Cache cache;
    static final Logger log = LoggerFactory.getLogger(BaseCacheWrapper.class);

    public BaseCacheWrapper(String cacheName){
        this.cache = PluginCacheManager.getCache(cacheName);
        if(this.cache == null){
            log.error("Can't create cache instance");
            throw new RuntimeException("Can't create cache instance"); 
        }        
    }

    protected Element createElement(String id, E object) {
        return new Element(id, object);
    } 
    
    public abstract void putInCache(String Id, E object);

    public abstract E getFromCache(String Id);

}