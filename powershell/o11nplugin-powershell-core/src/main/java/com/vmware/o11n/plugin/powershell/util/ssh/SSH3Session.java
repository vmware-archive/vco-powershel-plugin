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
package com.vmware.o11n.plugin.powershell.util.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.util.security.FileHandlerAccessRights;
import ch.dunes.util.security.FileHandlerAccessRightsFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.vmware.o11n.plugin.powershell.remote.impl.PowerShellTerminal;
import com.vmware.o11n.plugin.powershell.remote.impl.PowerShellTerminalResult;

public class SSH3Session extends SSH2AbstractSession {
    
    static final Logger log = LoggerFactory.getLogger(SSH3Session.class);

    private ChannelShell shellChannel;
    private PipedOutputStream shellOutStream;
    private PipedInputStream shellInStream;

    private String m_error;

    private String commandTerminator = "\n\n\n";

    /**
     * @param host
     * @param username
     */
    public SSH3Session(String host, String username) {
        super(host, username);
    }

    /**
     * @param host
     * @param username
     * @param port
     */
    public SSH3Session(String host, String username, int port) {
        super(host, username, port);
    }

    public String getCommandTerminator() {
        return commandTerminator;
    }

    public void setCommandTerminator(String commandTerminator) {
        this.commandTerminator = commandTerminator;
    }

    public void startShell() throws JSchException {
        checkConnection();
        shellChannel = (ChannelShell) getSession().openChannel("shell");
        shellChannel.setPty(false);
        shellOutStream = new PipedOutputStream();
        try {
            InputStream channelInputStream = new PipedInputStream(shellOutStream);
            shellChannel.setInputStream(channelInputStream);
            shellInStream = new PipedInputStream();
            shellChannel.setOutputStream(new PipedOutputStream(shellInStream));
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        Thread t = new Thread("SSH3Session-" + getSession().getUserName()) {
            @Override
            public void run() {
                try {
                    shellChannel.connect();
                } catch (JSchException e) {
                    //throw e;
                }
            }
        };

        t.setDaemon(true);
        t.start();
        log.debug("connected:" + shellChannel.isConnected());
        int maxPeriodsToWait = 5;
        while (!shellChannel.isConnected()) {
            try {
                log.debug("waiting for connected session");
                if (maxPeriodsToWait <= 0) {
                    break;
                }
                Thread.sleep(1000);
                maxPeriodsToWait--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.debug("connected:" + shellChannel.isConnected());
    }

    /**
     * Executes command on the remote machine.
     * @param command - the command to execute on the remote machine.
     * @return - the result as <String> e.g. what you would see if you were executing the command on a terminal.
     * @throws SSHException - when there is an error with the executed command e.g. the error stream has bytes in it  
     */
    public synchronized void sendShellCommandNoResult(String command) throws SSHException {
        command += commandTerminator;
        byte[] bytes = command.getBytes();
        try {
            shellOutStream.write(bytes);
            log.debug("Send command: " + command);
        } catch (IOException e) {
            log.error("Can't send shell command via SSH" + e.getMessage());
            e.printStackTrace();
        }
    }

    public PowerShellTerminalResult readResultFromInputStream() throws SSHException {
        PowerShellTerminalResult cmdResult = readResultFromInputStream(shellInStream);
        if (cmdResult.getError().trim().length() > 0 ){
            throw new SSHException(cmdResult.getError());
        }  
        return cmdResult;
    }

    private PowerShellTerminalResult readResultFromInputStream(InputStream inStream) {
        
        StringBuilder hostOutput = new StringBuilder();
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        StringBuilder current = null ;
        
        try {
            InputStreamReader inStreamReader = new InputStreamReader(inStream);
            BufferedReader r = new BufferedReader(inStreamReader);
            String str = null;
            do{
                str = r.readLine();
                if( str.equals(PowerShellTerminal.RESULT_DELIMITER_START)){
                    current = hostOutput; 
                    continue;
                } else if( str.equals(PowerShellTerminal.RESULT_DELIMITER_END)){
                    break;
                } else if ( PowerShellTerminal.ERRORS_START.equals(str) ){
                    current = error;
                    continue;
                } else if ( PowerShellTerminal.OUTPUT_START.equals(str) ){
                    current = output;
                    continue;
                } else if ( PowerShellTerminal.ERRORS_END.equals(str) ){
                    //TODO error id not in errors
                    current = hostOutput;
                    continue;
                } else if ( PowerShellTerminal.OUTPUT_END.equals(str) ){
                    //TODO error id not in output
                    current = hostOutput;
                    continue;
                }
                
                if(current != null && str != null ){
                    current.append(str).append('\n');
                }
            } while(str != null);
        } catch (IOException e) {
            throw new RuntimeException("Can't read result", e);
        }         
        return new PowerShellTerminalResult(error.toString(), output.toString(), hostOutput.toString());
    }

    @Override
    public void disconnect() {
        super.disconnect();
        try {
            if (shellInStream != null) {
                shellInStream.close();
            }
        } catch (IOException e) {
            log.warn("Can't close stream" + e.getMessage());
        }

        try {
            if (shellOutStream != null) {
                shellOutStream.close();
            }
        } catch (IOException e) {
            log.warn("Can't close stream" + e.getMessage());
        }
    }

    @Override
    protected void preConnect() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void preDisconnect() {
        // TODO Auto-generated method stub
        
    }

    public int getFile(String remoteFile, String localFile) {
        int result = 0;
        FileOutputStream fos = null;
        Channel channel = null;
        if ( remoteFile == null ) {
        	throw new NullPointerException("Remote file cannot be 'null'") ;
        }
        if ( localFile == null ) {
        	throw new NullPointerException("Local file cannot be 'null'") ;
        }
        // check if authorized to write the file
        File dir;
        if (new File(localFile).isDirectory()) {
        	dir = new File(localFile);
        } else {
        	dir =  new File(localFile).getParentFile();
        }
        if ( FileHandlerAccessRightsFactory.getDefaultFileHandlerAccessRights().hasRights(dir, FileHandlerAccessRights.Rights.write) == false) {
        	throw new RuntimeException("Permission denied on directory '"+dir.getAbsolutePath()+"' , write not allowed");
        }
        try {
    
            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }
    
            Session session = getSession();
    
            // exec 'scp -f rfile' remotely
            String command = "scp -f " + remoteFile;
            channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
    
            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();
    
            channel.connect();
    
            byte[] buf = new byte[1024];
    
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
    
            while (true) {
                int c = checkAck(in);
                if (c != 'C') {
                    result = 0;
                    break;
                }
    
                // read '0644 '
                in.read(buf, 0, 5);
    
                long filesize = 0L;
                while (true) {
                    if (in.read(buf, 0, 1) < 0) {
                    	// error
                    	break;
                    }
                    if (buf[0] == ' ')
                        break;
                    filesize = filesize * 10L + (long)(buf[0] - '0');
                }
    
                String file = null;
                for (int i = 0;; i++) {
                    in.read(buf, i, 1);
                    if (buf[i] == (byte)0x0a) {
                        file = new String(buf, 0, i);
                        break;
                    }
                }
    
                // System.out.println("filesize="+filesize+", file="+file);
    
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
    
                // read a content of lfile
                fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
                int foo;
                while (true) {
                    if (buf.length < filesize)
                        foo = buf.length;
                    else foo = (int)filesize;
                    foo = in.read(buf, 0, foo);
                    if (foo < 0) {
                    	// error
                    	break;
                    }
                    fos.write(buf, 0, foo);
                    filesize -= foo;
                    if (filesize == 0L)
                        break;
                }
                fos.close();
                fos = null;
    
                int ret = checkAck(in);
                if (ret != 0) {
                    return ret;
                }
    
                // send '\0'
                buf[0] = 0;
                out.write(buf, 0, 1);
                out.flush();
            }
        }
        catch (Exception e) {
            m_error = e.getMessage();
            result = -1;
        }
        finally {
            try {
                if (channel != null) {
                    channel.disconnect();
                }
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            // disconnect();
        }
        return result;
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        // 1 for error,
        // 2 for fatal error,
        // -1
        if (b == 0)
            return b;
        if (b == -1)
            return b;
    
        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char)c);
            } while (c != '\n');
            if (b == 1) { // error
                m_error = sb.toString();
                return -1;
            }
            if (b == 2) { // fatal error
                m_error = sb.toString();
                return -2;
            }
        }
        return b;
    }

}