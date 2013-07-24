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

import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.model.cache.SessionCacheListener;

public class PluginCacheManager {
    private static final Logger log = LoggerFactory.getLogger(PluginCacheManager.class);

    protected static CacheManager CACHE_MANAGER;
    
    static {
        
        initCacheManager(300, true, false);
    }

    private static void initCacheManager(int maxElementsInMemory, boolean eternal, boolean skipFile) {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(PluginCacheManager.class.getClassLoader());
            try {
                URL resource = PluginCacheManager.class.getResource("ehcache.xml");                  
                if (null != resource) {
                    CACHE_MANAGER = new CacheManager(resource);
                    log.info("Cache settings taken from:" + resource);
                } 
            } catch (CacheException e) {
                throw new RuntimeException("Unable to locate plugin cache configuration file.");
            }
            
            initDataCache();
            initSessionCache();
            
        } catch (Exception e) {
            log.error("Unable to initialize session cache", e);
        }finally{
            Thread.currentThread().setContextClassLoader(orig);
        }
    }

    private static void initDataCache() {
        
        Cache memoryOnlyCache = new Cache(CacheConstants.DATA_CACHE, PowerShellPluginConfiguration.getInstance().getInvocationResultMaxEntriesNumber(), false, false, 0, PowerShellPluginConfiguration.getInstance().getDataCacheTimeToIdleSeconds());
        CACHE_MANAGER.addCache(memoryOnlyCache);
    }
    
    private static void initSessionCache() {
        Cache memoryOnlyCache = new Cache(CacheConstants.SESSION_CACHE, PowerShellPluginConfiguration.getInstance().getSessionMaxNumber(), false, false, 0, PowerShellPluginConfiguration.getInstance().getSessionCacheTimeToIdleSeconds());
        memoryOnlyCache.getCacheEventNotificationService().registerListener(new SessionCacheListener()); 
        CACHE_MANAGER.addCache(memoryOnlyCache);
    }    
    
    public static Cache getOrCreateCache(String cacheName) {

        if (!CACHE_MANAGER.cacheExists(cacheName)){
            log.debug("Cache created:" + cacheName);
            CACHE_MANAGER.addCache(cacheName);
        }
        
        return CACHE_MANAGER.getCache(cacheName);
    }


    public static void removeCache(String cacheName) {
        if (CACHE_MANAGER.cacheExists(cacheName)) {
            CACHE_MANAGER.removeCache(cacheName);
            log.debug("Removed cache :" + cacheName);
        }        
    }

    public static Cache getCache(String cacheName) {
        return CACHE_MANAGER.getCache(cacheName);
    }

}
