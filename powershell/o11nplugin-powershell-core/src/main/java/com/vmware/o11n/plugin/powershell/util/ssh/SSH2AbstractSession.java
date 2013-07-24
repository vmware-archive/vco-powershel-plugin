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
/**
 * 
 */
package com.vmware.o11n.plugin.powershell.util.ssh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author David
 * 
 */
public abstract class SSH2AbstractSession {
    private Session m_session;
    private ByteArrayOutputStream m_out;
    private ByteArrayOutputStream m_err;

    private ChannelExec m_channel;
    private int m_exitCode = -1;

    private String m_username;

    private int m_port;
    private String m_host;
    // infos.put("StrictHostKeyChecking", "false");
    private Hashtable<String, Object> infos = new Hashtable<String, Object>();

    static {
        Hashtable config = new Hashtable() ;
        config.put("cipher.s2c", "aes128-cbc,aes256-cbc,3des-cbc,blowfish-cbc");
        config.put("cipher.c2s", "aes128-cbc,aes256-cbc,3des-cbc,blowfish-cbc");
        JSch.setConfig(config) ;
    }
    
    public SSH2AbstractSession(String host, String username, int port) {
        super();
        m_username = username;
        m_host = host;
        m_port = port;
    }

    public SSH2AbstractSession(String host, String username) {
        this(host, username, 22);
    }

    protected void checkConnection() throws JSchException {
        if (m_session == null) {
            throw new JSchException("Not connected !");
        }
    }

    protected Session getSession() throws JSchException {
        checkConnection() ;
        return m_session;
    }

    public void setInfo(String key, Object value) {
        infos.put(key, value);
    }

    public Object getInfo(String key) {
        return infos.get(key);
    }

    public void connectWithPasswordOrIdentity(boolean isPassword, String password, String identityPath) throws JSchException, FileNotFoundException{
        if(isPassword){
            connectWithPassword(password);
        } else {
            connectWithIdentity(identityPath, password);
        }
    }
    
    public void connectWithPassword(String password) throws JSchException {
        if (m_session != null) {
            m_session.disconnect();
        }
        preConnect();
        JSch jsch = new JSch();

        Hashtable infos = new Hashtable();

        SSH2User userInfo = new SSH2User(m_username, password);
        m_session = jsch.getSession(userInfo.getUsername(), m_host, m_port);
        m_session.setConfig(infos);
        m_session.setPassword(userInfo.getPassword());
        m_session.setUserInfo(userInfo);
        m_session.connect();
    }
    
    public void connectWithIdentidy(String identityPath, String passPhrase) throws JSchException,FileNotFoundException {
        connectWithIdentity(identityPath, passPhrase);
    }

    public void connectWithIdentity(String identityPath, String passPhrase) throws JSchException,FileNotFoundException {
        if (identityPath == null || new File(identityPath).exists() == false) {
            throw new FileNotFoundException("Identity file not found !") ;
        }
        if (m_session != null) {
            m_session.disconnect();
        }
        preConnect();
        JSch jsch = new JSch();
        if (passPhrase != null && passPhrase.length() >0) {
            jsch.addIdentity(identityPath, passPhrase);
        }
        else {
            jsch.addIdentity(identityPath);
        }

        SSH2User userInfo = new SSH2User(m_username, passPhrase);
        m_session = jsch.getSession(m_username, m_host, m_port);
        m_session.setConfig(infos);
        m_session.setUserInfo(userInfo);
        m_session.connect();
    }

    public void disconnect() {
        if (m_session != null) {
            preDisconnect();
            m_session.disconnect();
            m_session = null;
        }
    }

    public void setPort(int p) {
        m_port = p;
    }

    public int getPort() {
        return m_port;
    }

    protected abstract void preConnect();
    protected abstract void preDisconnect();
}
