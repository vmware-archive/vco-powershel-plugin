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

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTPS;

import com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsWinRmPowerShellConnection;
import com.vmware.o11n.plugin.powershell.remote.impl.winrm.OverthereWinRMPowerShell;
import com.vmware.o11n.plugin.powershell.remote.impl.winrm.WinRmCapturingOverthereProcessOutputHandler;
import com.vmware.o11n.plugin.powershell.util.ssh.SSHException;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;

public class WinRmPowerShellTerminal extends BasePowerShellTerminal {
    
    private static final String POWERSHELL_BROKER_INITIALIZED_TRUE = "POWERSHELL.BROKER.INITIALIZEd=TRUE";
    private static final String START_POWERSHELL_COMMAND = "powershell -Command \" ^&{do{ $cmd=Read-Host; if($cmd){  $bytes = [System.Convert]::FromBase64String($cmd); $decodedCmd = [System.Text.Encoding]::UTF8.GetString($bytes); Invoke-Expression $decodedCmd} }while($true);}\"";

    private CifsWinRmPowerShellConnection connection;
    private BrokerOutputHandler capturingHandler;
    private ConnectionProps connectionProps = null;

    public WinRmPowerShellTerminal(String host, String transportProtocol, Integer port,String username, String authenticationSchema) {
        capturingHandler = new BrokerOutputHandler();
        connectionProps = new ConnectionProps(host, username, transportProtocol, port, authenticationSchema);

    }

    @Override
    public void connectWithPassword(String password) {
        ConnectionOptions options = createCifsWinRmHttpOptions(connectionProps, password);
        connection = (CifsWinRmPowerShellConnection)OverthereWinRMPowerShell.getConnection("CifsWinRmPowerShell", options);

    }

    @Override
    public void startShellWithSerialization() {
        CmdLine cmd = CmdLine.build(START_POWERSHELL_COMMAND);
        connection.open(cmd);
        
        WinRmCapturingOverthereProcessOutputHandler initializationHandler = new WinRmCapturingOverthereProcessOutputHandler(){
            
            private boolean isDone;

            public synchronized void handleOutputLine(final String line) {
            }

            @Override
            public void handleOutput(char c) {
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

            @Override
            public void handleOutputChunk(String stdout) {
                if (stdout.contains(POWERSHELL_BROKER_INITIALIZED_TRUE) ) {
                    isDone = true;
                }                        
                
            }
           };
        
        String broker = encodeToBase64(SCRIPT_SERIALIZER_METHODS + "\r\n" + "echo '" + POWERSHELL_BROKER_INITIALIZED_TRUE + "'\r\n"); 
        connection.sendCommandToStream(broker + "\r\n", true, initializationHandler);
    }

    @Override
    public PowerShellTerminalResult sendShellCommand(String script, int levelOfRecursion) throws SSHException {
        capturingHandler.reset();
        prepareAndSendShellCommand(script, levelOfRecursion);
        return readResult();
    }

    private void prepareAndSendShellCommand(String script, int levelOfRecursion) {
        String modifiedCmd = SCRIPT_SERIALIZER_METHOD_NAME + " '" + escape(script) + "' ";
        if(levelOfRecursion > 0){
            modifiedCmd  += levelOfRecursion;
        }
        modifiedCmd += "\r\n\r\n\r\n";
        
        //TODO decide what to do with exit code
        modifiedCmd = encodeToBase64(modifiedCmd) + "\r\n";
        connection.sendCommandToStream(modifiedCmd, true, capturingHandler);
    }

    private PowerShellTerminalResult readResult() throws SSHException {
        PowerShellTerminalResult cmdResult = capturingHandler.getInvResult();
        if (cmdResult.getError().trim().length() > 0 ){
            throw new SSHException(cmdResult.getError());
        }  

       return cmdResult;
    }
    
    @Override
    public void disconnect() {
        connection.close();
    }

    private static ConnectionOptions createCifsWinRmHttpOptions(ConnectionProps connectionProps, String password) {
        return connectionProps.getConnectionOptions(password);
    }
    
    
    public static class ConnectionProps {
        public static final String AUTHENTICATION_SCHEMA = "authenticationSchema";
        
		private String host;
        private String username;
        private String transportProtocol;
        private Integer port;
		private String authenticationSchema;

        public ConnectionProps(String host, String username, String transportProtocol, Integer port, String authenticationSchema) {
            if(port != null && (port < 1 || port >65535)){
                throw new IllegalArgumentException("Port is not valid:" + port);
            }
            this.host = host;
            this.username = username;
            this.transportProtocol = transportProtocol;
            this.port = port;
            this.authenticationSchema = authenticationSchema;
        }
        
        public ConnectionOptions getConnectionOptions(String password){
            ConnectionOptions partialOptions = new ConnectionOptions();
            partialOptions.set(OPERATING_SYSTEM, WINDOWS);
            if(transportProtocol != null && WINRM_HTTPS.name().endsWith(transportProtocol)){
                partialOptions.set(CONNECTION_TYPE, WINRM_HTTPS);
                partialOptions.set(PORT, DEFAULT_WINRM_HTTPS_PORT);
            } else if (transportProtocol != null && WINRM_HTTP.name().endsWith(transportProtocol)){
                partialOptions.set(CONNECTION_TYPE, WINRM_HTTP);
                partialOptions.set(PORT, DEFAULT_WINRM_HTTP_PORT);
            } else {
                throw new IllegalArgumentException("Wrong parameter provided for transportProtocol:" + transportProtocol);
            }
            
            if(port != null){
                partialOptions.set(PORT, port);
            }
            
            partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
            partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
            
            partialOptions.set(ConnectionOptions.ADDRESS, host);
            partialOptions.set(USERNAME, username);
            partialOptions.set(PASSWORD, password);
            partialOptions.set(AUTHENTICATION_SCHEMA, authenticationSchema);
            return partialOptions;        
        }
    }
}
