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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.model.Session;

public class SessionCacheListener implements CacheEventListener{

    private static final Logger log = LoggerFactory.getLogger(SessionCacheListener.class);
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element)
            throws CacheException {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element)
            throws CacheException {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element)
            throws CacheException {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        //means that session has to be closed because ehcache removes the entry 
        Session session = (Session)element.getObjectValue();
        session.disconnect();
        log.debug("Session expired sessionId:" + session.getSessionId());
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        // TODO Auto-generated method stub
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }
}
