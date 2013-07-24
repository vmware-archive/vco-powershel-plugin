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

import java.io.Serializable;

import com.vmware.o11n.plugin.powershell.remote.IPowerShellTerminal;



public interface Session extends Serializable {

    //Add Command to current pipeline
    void addCommand(Command command);

    //Add Command to current pipeline
    Command addCommandFromString(String command);

    //Start execution  of command in current pipeline 
    InvocationResult invokePipeline();

    /**
     * Executes script without adding it to current pipeline
     * @param script - the script to execute
     * @return result from execution
     */
    InvocationResult invoke(String script);

    /**
     * Executes script without adding it to current pipeline
     * @param script - the script to execute
     * @param levelOfRecursion - the recursion level to use when serializing result objects from remote execution of
     * PowerShell script specified in <parameter>script</parameter>  
     * @return result from execution
     */
    InvocationResult invoke(String script, int levelOfRecursion);

    /**
     * @return the SessionId that uniquely identifies this Session
     */
    String getSessionId();

    void disconnect();

    
    // Used from client to decide whether to execute command immediately
    // or to delay execution until endPipeline is invoked.
    boolean isInPipeline();

    /**
     * Starts a pipeline in the context of this session
     * the inPipeline flag is set to true
     */
    void startPipeline();

    /**
     * Executes the current pipeline and flushes everything from it.
     * the inPipeline flag is set to false
     */
    void endPipeline();
    
    String getUserName();
    
    IPowerShellTerminal getTerminal();

    void setTerminal(IPowerShellTerminal iPowerShellTerminal);
}
