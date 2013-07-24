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

import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.CacheConstants;
import com.vmware.o11n.plugin.powershell.model.RemotePSObject;

public class InvokationResultsCache extends BaseCacheWrapper<RemotePSObject>{
    
    static final Logger log = LoggerFactory.getLogger(InvokationResultsCache.class);
    
    public InvokationResultsCache(){
        super(CacheConstants.DATA_CACHE);      
    }

    public InvokationResultsCache(String cacheName){
        super(cacheName);      
    }       

    @Override
    public void putInCache(String id, RemotePSObject object) {
        if (object != null) {
            cache.put(createElement(id, object));
        }
    }    
    
    @Override
    public RemotePSObject getFromCache(String id) {
        if (id != null) {
            Element element = cache.get(id);
            if(element != null){
                return (RemotePSObject)element.getObjectValue();
            }
        }
        return null;
    }     
 
}