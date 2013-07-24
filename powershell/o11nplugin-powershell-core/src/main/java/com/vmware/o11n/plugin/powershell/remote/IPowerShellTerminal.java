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

package com.vmware.o11n.plugin.powershell.remote;

import com.vmware.o11n.plugin.powershell.remote.impl.PowerShellTerminalResult;
import com.vmware.o11n.plugin.powershell.util.ssh.SSHException;

public interface IPowerShellTerminal {

    final String REGEX_FOR_REFID = "@REFIDEND@";
    
    void connectWithPassword(String password);

    void startShellWithSerialization();

    /**
     * Sends command for remote execution with the default level of recursion
     * @param cmd - the command to send for execution to the remote power shell machine
     * @return - string result of command execution.
     * @throws SSHException - in case of error either in execution or in communication
     */    
    PowerShellTerminalResult sendShellCommand(String script) throws SSHException;
    
    /**
     * Sends command for remote execution with the specified level of recursion for serializing objects
     * @param cmd - the command to execute
     * @param levelOfRecursion - the level to which to recursively serialize the result objects after execution of <parameter>cmd</parameter> script
     * @return - JSON String with result serialized up to the level specified by <parameter>levelOfRecursion</parameter> parameter
     * @throws SSHException - in case of error either in execution or in communication
     */
    PowerShellTerminalResult sendShellCommand(String script, int levelOfRecursion) throws SSHException;

    void disconnect();

}
