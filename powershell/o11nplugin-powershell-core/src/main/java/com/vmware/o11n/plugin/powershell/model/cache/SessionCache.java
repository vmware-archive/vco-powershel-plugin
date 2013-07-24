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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.CacheConstants;
import com.vmware.o11n.plugin.powershell.model.Session;
import com.vmware.o11n.plugin.powershell.remote.IPowerShellTerminal;

public class SessionCache extends BaseCacheWrapper<Session>{

    private Map<String, IPowerShellTerminal> terminals = new HashMap<String, IPowerShellTerminal>();
    
    static final Logger log = LoggerFactory.getLogger(SessionCache.class);
    
    public SessionCache(){
        super(CacheConstants.SESSION_CACHE);      
    }

    public SessionCache(String cacheName){
        super(cacheName);      
    }    
    
    /* (non-Javadoc)
     * @see com.vmware.o11n.plugin.powershell.model.cache.ICacheWrapper#putInCache(com.vmware.o11n.plugin.powershell.model.Session)
     */
    @Override
    public void putInCache(String id, Session session) {
        if (session != null) {
            cache.put(createElement(id, session));
            terminals.put(session.getSessionId(), session.getTerminal());
        }
    }    
    
    /* (non-Javadoc)
     * @see com.vmware.o11n.plugin.powershell.model.cache.ICacheWrapper#getFromCache(java.lang.String)
     */
    @Override
    public Session getFromCache(String sessionId) {
        if (sessionId != null) {
            Element element = cache.get(sessionId);
            if(element != null){
                Session session = (Session)element.getObjectValue();
                session.setTerminal(terminals.get(session.getSessionId()));
                return session;
            }
        }
        return null;
    }     
    
    public List<Session> getSessionsFromCache() {
        List<Session> result = new ArrayList<Session>();
        List keys = cache.getKeysWithExpiryCheck();
        for (Object key : keys) {
            Session session = (Session)cache.get(key).getObjectValue();
            session.setTerminal(terminals.get(session.getSessionId()));
            result.add(session);
        }
        return result;
    }        
    
    public boolean hasValidSessions() {
        return cache.getKeysWithExpiryCheck().size() > 0;
    }
    
    public void invalidateAllSessions() {
        setAllActiveSessionsTimeToLive(-1);
    }        
    
    void setAllActiveSessionsTimeToLive(int seconds) {
        List keys = cache.getKeysWithExpiryCheck();
        for (Object key : keys) {
            cacheSetTimeToLive((String)key, seconds);
        }
    }         

    public void removeFromCache(Session session) {
        if(session != null){
            cache.remove(session.getSessionId());
            terminals.remove(session.getSessionId());
        }
    }
    
    /**
     * Sets the TimeToLive for the element with provided sessionId in the current cache
     * @param sessionId - the id of the element to which to set TTL 
     * @param seconds - the TTL in seconds. If negative or 0 is passed then the default value is used
     * @see CacheConstants.TIME_TO_LIVE
     */
    public void cacheSetTimeToLive(String sessionId, int seconds) {
        Element element = cache.get(sessionId);
        if(element != null){
            element.setTimeToLive((seconds <= 0) ? CacheConstants.TIME_TO_LIVE : seconds);
            Session session = (Session)element.getObjectValue();
            terminals.remove(session.getSessionId());
        }
    }        
    
    int getCachedTerminalsCount(){
       return terminals.size(); 
    }
    
    int getValidSessionsCount(){
        return cache.getKeysWithExpiryCheck().size(); 
    }

    void removeAll() {
        cache.removeAll();
    }  
}