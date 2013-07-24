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
package com.vmware.o11n.plugin.powershell.scripting;

import com.vmware.o11n.plugin.powershell.model.Command;
import com.vmware.o11n.plugin.powershell.model.Host;
import com.vmware.o11n.plugin.powershell.model.InvocationResult;
import com.vmware.o11n.plugin.powershell.model.Session;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

@VsoObject(description="Represents a session to the remote PowerShell machine, that can be used to run scripts and Cmdlets." +
		                " Also users can benefit from the pipeline concept by demarcating pipeline boundaries with" +
		                " startPipeline() and endPipeline()")
public class PowerShellSession {
    private String sessionId;
    private Host host;

    public PowerShellSession(Session impl, Host host) {
        this.sessionId = impl.getSessionId();
        this.host = host;
    }

    @VsoMethod (description ="Returns the session id")
    public String getSessionId(){
        return sessionId;
    }

    private Session getSessionImpl(){
        return host.getSession(sessionId);
    }

    @VsoMethod (description = "Invokes commands currently in pipeline, and remove them from the pipeline.")
    public InvocationResult invokePipeline(){
        return getSessionImpl().invokePipeline();
    }

    @VsoMethod (description= "Invokes powershell script without adding it to pipeline.")
    public InvocationResult invokeScript(String script){
        return getSessionImpl().invoke(script);
    }
 
    @VsoMethod (description = "Adds command to pipeline" )
    public void addCommand(Command command) {
        getSessionImpl().addCommand(command);
    }

    @VsoMethod (description = " Creates command from provided string and adds it to pipeline.")
    public Command addCommandFromString(String command) {
        return getSessionImpl().addCommandFromString(command);
    }    
    
    public boolean inPipeline() {
        return getSessionImpl().isInPipeline();
    }    

    public void startPipeline() {
        getSessionImpl().startPipeline();
    }    
    
    public void endPipeline() {
        getSessionImpl().endPipeline();
    }    
    
}