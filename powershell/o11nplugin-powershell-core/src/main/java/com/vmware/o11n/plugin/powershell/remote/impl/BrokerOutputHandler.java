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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.vmware.o11n.plugin.powershell.remote.impl.winrm.WinRmCapturingOverthereProcessOutputHandler;

public class BrokerOutputHandler implements WinRmCapturingOverthereProcessOutputHandler {

    private StringBuilder output = new StringBuilder();
   
    private boolean isDone;
    
    @Override
    public void handleOutputLine(String line) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleErrorLine(String line) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return isDone;
    }


    public PowerShellTerminalResult getInvResult() {
        
        
        return parseOutput(output.toString());
    }
    
    private PowerShellTerminalResult parseOutput(String outputStream ) {
            
        
        StringBuilder hostOutput = new StringBuilder();
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        StringBuilder current = null ;
        
        try {
            BufferedReader r = new BufferedReader(new StringReader(outputStream));
            String str = null;
            do{
                str = r.readLine();
                if( str.equals(PowerShellTerminal.RESULT_DELIMITER_START)){
                    current = hostOutput; 
                    continue;
                } else if( str.equals(PowerShellTerminal.RESULT_DELIMITER_END)){
                    break;
                } else if ( str.equals(PowerShellTerminal.ERRORS_START ) ){
                    current = error;
                    continue;
                } else if ( str.equals(PowerShellTerminal.OUTPUT_START ) ){
                    current = output;
                    continue;
                } else if ( str.equals(PowerShellTerminal.ERRORS_END ) ){
                    //TODO error id not in errors
                    current = hostOutput;
                    continue;
                } else if ( str.equals(PowerShellTerminal.OUTPUT_END ) ){
                    //TODO error id not in output
                    current = hostOutput;
                    continue;
                }
                
                if ( current != null ) {
                    current.append(str).append('\n');
                }
            } while(str != null);
        } catch (IOException e) {
            throw new RuntimeException("Can't read result", e);
        }         
        return new PowerShellTerminalResult(error.toString(), output.toString(), hostOutput.toString());
    }

    public void reset() {
        output = new StringBuilder();
        isDone = false;
    }

    @Override
    public void handleOutput(char c) {
        
    }

    @Override
    public void handleOutputChunk(String stdoutChunk) {
        output.append(stdoutChunk);
        if ( output.toString().contains('\n' + PowerShellTerminal.RESULT_DELIMITER_END + '\n') ){
            isDone = true;
        } 
    }

}
