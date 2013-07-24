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

package com.vmware.o11n.plugin.powershell.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.hibernate.engine.SessionImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.CacheConstants;
import com.vmware.o11n.plugin.powershell.model.cache.BaseCacheWrapper;
import com.vmware.o11n.plugin.powershell.model.cache.InvokationResultsCache;
import com.vmware.o11n.plugin.powershell.remote.IPowerShellTerminal;
import com.vmware.o11n.plugin.powershell.remote.impl.PowerShellTerminalResult;

public abstract class BaseSession implements Session {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(BaseSession.class);

    private String sessionId;
    
    private transient IPowerShellTerminal powerShellTerminal;

    private Pipeline pipeline = new Pipeline();

    private boolean inPipeline;
    
    private String userName;

    private transient BaseCacheWrapper<RemotePSObject> cachedInvocationResults = new InvokationResultsCache(CacheConstants.DATA_CACHE); 

    /**
     * Opens a remote session to the power shell machine
     * @param host
     * @param username
     * @param password
     * @throws PowerShellException - thrown in case the session can't be open
     */
    public BaseSession(String host, String username, String password, IPowerShellTerminal psTerminal) throws PowerShellException {
        if(psTerminal == null){
            throw new IllegalArgumentException("powerShellTerminal parameter is not allowed to be null");
        }
        this.userName = username;
        this.sessionId = generateUniqueSessionId(host);
        this.powerShellTerminal = psTerminal;
        try {
            this.powerShellTerminal.connectWithPassword(password);
            this.powerShellTerminal.startShellWithSerialization();
        } catch (Exception e) {
            String msg = e.getMessage();
            if ( msg == null ){
                msg  = "Can't establish session to remote PowerShell machine.";
            } 
            throw new PowerShellException(msg, e);
        }
    }
    
    
    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void disconnect() {
        if (powerShellTerminal != null) {
            try {
                powerShellTerminal.disconnect();
                log.debug("Session closed " + sessionId);
            } catch (Exception e){
                log.warn("Errors found while closing session " + sessionId + ".");
            }
        } else {
            log.debug("PowerShell terminal is not set for session " + sessionId);
        }
    }

    private InvocationResult wrapResultIntoObject(PowerShellTerminalResult result, String errorMessage) {
        RemotePSObject psObj = null;
        String hostOutput = null;
        List<String> errors = null;

        if (result!= null){
            hostOutput = result.getHostOutput();
            if  ( ! StringUtils.isEmpty( result.getOutput())){
                String[] tmp  = result.getOutput().split(IPowerShellTerminal.REGEX_FOR_REFID, 2);
                psObj = new RemotePSObject(tmp[1], sessionId);
                psObj.setRefId(tmp[0]);
            }
        }

        InvocationState status = InvocationState.Completed;
        if (errorMessage != null) {
            errors = new ArrayList<String>();
            errors.add(errorMessage);
            status = InvocationState.Failed;
        }

        return new InvocationResult(status, errors, psObj, hostOutput);
    }


    private String generateUniqueSessionId(String host) {
        return UUID.randomUUID().toString();
    }

    @Override
    public void addCommand(Command command) {
        pipeline.addCommand(command);
    }

    @Override
    public Command addCommandFromString(String command) {
        return pipeline.addCommandFromString(command);
    }
    

    @Override
    public InvocationResult invokePipeline() {
        InvocationResult res = invoke( pipeline.getScript() );
        pipeline = new Pipeline();
        return res;
    }

    @Override
    public InvocationResult invoke(String script) {
        return invoke(script, -1);
    }
    
    @Override
    public InvocationResult invoke(String script, int levelOfRecursion) {
        log.debug("Invoke script :" + script);
        PowerShellTerminalResult result = null;
        String errorMessage = null;
        try {
            result = powerShellTerminal.sendShellCommand(script, levelOfRecursion);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error(errorMessage, e);
        }
        InvocationResult obj = wrapResultIntoObject(result, errorMessage);
        RemotePSObject res = obj.getResults();
        if(res != null){
            cachedInvocationResults.putInCache(res.getRefId(), res);
        }
        log.debug("Invoke script : Completed");
        return obj;
    }


    // Marker do we want to execute each command
    // or delay it's execution in pipeline
    @Override
    public void startPipeline(){
        if (inPipeline){
            throw new PowerShellException("Pipeline already started");
        } 
        inPipeline = true;
    }

    @Override
    public void endPipeline(){
        if (!inPipeline){
            throw new PowerShellException("Missing start pipeline.");
        } 
        
        inPipeline = false;
    }

    @Override
    public boolean isInPipeline(){
        return inPipeline;
    }
    
    @Override
    public String getUserName(){
        return userName;
    }
    
    @Override
    public IPowerShellTerminal getTerminal() {
        return powerShellTerminal;
    }
    
    @Override
    public void setTerminal(IPowerShellTerminal powerShellTerminal) {
        this.powerShellTerminal = powerShellTerminal;
    }
    
    public Object getResultById(String objId) {
        RemotePSObject resultFromCache = cachedInvocationResults.getFromCache(objId);
        if (resultFromCache != null) {
            log.debug("RemotePSObject with id:" + objId + " taken from cache. sessionId:" + sessionId);
            return resultFromCache;
        }
        
        log.debug("RemotePSObject with id:" + objId + " taken from remote PowerShell host. sessionId:" + sessionId);
        return invoke("getVarByRef " + objId).getResults();
    }
}
