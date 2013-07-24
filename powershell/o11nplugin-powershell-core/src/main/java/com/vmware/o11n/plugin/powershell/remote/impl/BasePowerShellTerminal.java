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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.login.Base64;

import com.vmware.o11n.plugin.powershell.remote.IPowerShellTerminal;
import com.vmware.o11n.plugin.powershell.util.ssh.SSHException;

public abstract class BasePowerShellTerminal implements IPowerShellTerminal {

    static final Logger log = LoggerFactory.getLogger(BasePowerShellTerminal.class);
    
    

    protected static final String SCRIPT_SERIALIZER_METHOD_NAME = "broker_serialize";


    public static final String RESULT_DELIMITER_START = "============RESULT_DELIMITER_START==========";
    public static final String RESULT_DELIMITER_END = "============RESULT_DELIMITER==========";
    public static final String OUTPUT_START = "============OUTPUT_START==========";
    public static final String OUTPUT_END = "============OUTPUT_END============";
    public static final String ERRORS_START = "============ERRORS_START==========";
    public static final String ERRORS_END = "============ERRORS_END============";
    
    private static final String SERIALIZER_SCRIPT_NAME = "Serializer.ps1_template";
    
    public static final String SCRIPT_SERIALIZER_METHODS;

    protected String startShellCommand = null;
    
    static{
        String templateScript = "";
        try {
            templateScript = IOUtils.toString(BasePowerShellTerminal.class.getResourceAsStream(SERIALIZER_SCRIPT_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
        templateScript = replacePlaceholderVariablesWithConstants(templateScript);
        SCRIPT_SERIALIZER_METHODS = templateScript;
    }

    protected static String encodeToBase64(String templateScript) {
        return new String(Base64.encode(templateScript.getBytes()));
    }    
    
    private static String replacePlaceholderVariablesWithConstants(String templateScript) {
        templateScript = templateScript.replace("{#SCRIPT_SERIALIZER_METHOD_NAME#}", SCRIPT_SERIALIZER_METHOD_NAME);
        templateScript = templateScript.replace("{#SCRIPT_SERIALIZER_METHOD_NAME#}", SCRIPT_SERIALIZER_METHOD_NAME);
        templateScript = templateScript.replace("{#ERRORS_START#}", ERRORS_START);
        templateScript = templateScript.replace("{#ERRORS_END#}", ERRORS_END);
        templateScript = templateScript.replace("{#OUTPUT_START#}", OUTPUT_START);
        templateScript = templateScript.replace("{#OUTPUT_END#}", OUTPUT_END);
        templateScript = templateScript.replace("{#RESULT_DELIMITER#}", RESULT_DELIMITER_END);
        return templateScript;
    }
    

    public void setStartShellCommand(String startShellCommand) {
        this.startShellCommand = startShellCommand;
    }
    
    @Override
    public PowerShellTerminalResult sendShellCommand(final String cmd) throws SSHException {
        return sendShellCommand(cmd, -1);
    }       
        
    protected static String escape(String command) {
        //replacing some strange single quotes
        command = command.replace("\u0091", "'");
        command = command.replace("\u0092", "'");
        command = command.replace("\u2018", "'");
        command = command.replace("\u2019", "'");
        return command.replace("'", "''");
    }    
}
