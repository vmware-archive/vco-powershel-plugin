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

package com.vmware.o11n.plugin.powershell.remote.impl;

import com.jcraft.jsch.JSchException;
import com.vmware.o11n.plugin.powershell.util.ssh.SSH3Session;
import com.vmware.o11n.plugin.powershell.util.ssh.SSHException;

public class PowerShellTerminal extends BasePowerShellTerminal{

    private static final String COMMAND_START_SHELL_OPENSSH = "powershell -NonInteractive -";
    protected SSH3Session sshSession;
    
    public PowerShellTerminal(String host, String username) {
        this.sshSession = new SSH3Session(host, username);
        this.setStartShellCommand(COMMAND_START_SHELL_OPENSSH);
    }
    
    @Override
    public PowerShellTerminalResult sendShellCommand(final String cmd, int levelOfRecursion) throws SSHException {
        String modifiedCmd = SCRIPT_SERIALIZER_METHOD_NAME + " '" + escape(cmd) + "' ";
        if(levelOfRecursion > 0){
            modifiedCmd  += levelOfRecursion;
        }
        sshSession.sendShellCommandNoResult(modifiedCmd);
        return sshSession.readResultFromInputStream();
    }
    
    @Override
    public void connectWithPassword(String password) {
        try {
            sshSession.connectWithPassword(password);
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startShellWithSerialization(){
        try {
            sshSession.startShell();
            String cmd = startShellCommand;
            sshSession.sendShellCommandNoResult(cmd);
   
            cmd = SCRIPT_SERIALIZER_METHODS;
            sshSession.sendShellCommandNoResult(cmd);
            sshSession.sendShellCommandNoResult("\r\n\r\n");

        } catch (Exception e) {
            new RuntimeException("Can't start shell with serialization", e);
        }
    }    
    
    @Override
    public void disconnect() {
        sshSession.disconnect();
    }    
}
