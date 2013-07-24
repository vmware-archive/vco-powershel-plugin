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

import com.jcraft.jsch.UserInfo;

/**
 * Title: SSH2User.java <br>
 * Description: <br>
 * Copyright: Copyright (c) 2005 <br>
 * Company: Dunes Technologies SA <br>
 * 
 * @author David Saradini
 */

/**
 *
 */
public class SSH2User implements UserInfo {
    public String m_username;
    public String m_password;

    public SSH2User(String username, String password) {
        m_username = username;
        m_password = password;
    }

    public String getPassphrase() {
        return null;
    }

    public String getPassword() {
        return m_password;
    }

    public String getUsername() {
        return m_username;
    }

    public boolean promptPassword(String message) {
        //System.out.println("promptPassword-ASK : "+message);
        return true;
    }

    public boolean promptPassphrase(String message) {
        //System.out.println("promptPassphrase-ASK : "+message);
        return true;
    }

    public boolean promptYesNo(String message) {
        //System.out.println("promptYesNo-ASK : "+message);
        //System.out.println("ANSWER : yes");
        return true;
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * implements UIKeyboardInteractive to have access to this method.
     * we should implement the interaction.
     * @param destination
     * @param name
     * @param instruction
     * @param prompt
     * @param echo
     * @return
     */
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {

         System.out.println("promptKeyboardInteractive");
         System.out.println("destination: "+destination);
         System.out.println("name: "+name);
         System.out.println("instruction: "+instruction);
         System.out.println("prompt.length: "+prompt.length);
         System.out.println("prompt: "+prompt[0]);

         return null ;
    }

}
