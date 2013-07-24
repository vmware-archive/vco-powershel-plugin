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

package com.vmware.o11n.plugin.powershell;

public interface Constants {
	public static final String FINDER_POWER_SHELL_PLUGIN = "PS_PLUGIN";
    public static final String FINDER_POWER_SHELL_HOST = "PowerShellHost";
    public static final String FINDER_POWER_SHELL_SNAPIN_ROOT = "PowerShellSnapInRoot";
    public static final String FINDER_POWER_SHELL_SNAPIN = "PowerShellSnapIn";
    public static final String FINDER_POWER_SHELL_CMDLET = "PowerShellCmdlet";
    public static final String FINDER_POWER_SHELL_SESSION = "PowerShellSession";
    public static final String FINDER_POWER_SHELL_REMOTE_PS_OBJECT = "PowerShellRemotePSObject";
	
	public static final String RELATION_POWER_SHELL_HOST = "PowerShellHosts";
    public static final String RELATION_POWER_SHELL_SESSIONS = "PowerShellSessions";
    public static final String RELATION_HOST_HAS_SNAPIN_ROOT = "HostHasSnapinRoot";
    public static final String RELATION_SNAPINROOT_HAS_SNAPIN = "SnapinRootHasSnapin";
    public static final String RELATION_SNAPIN_HAS_CMDLET= "SnapinHasCmdlet";
    public static final String PLUGIN_NAME = "PowerShell";
    
    
    public static final String TYPE_POWERSHELL_HOST = String.format("%s:%s", Constants.PLUGIN_NAME, Constants.FINDER_POWER_SHELL_HOST);
    public static final String TYPE_POWER_SHELL_REMOTE_PS_OBJECT = String.format("%s:%s", Constants.PLUGIN_NAME, Constants.FINDER_POWER_SHELL_REMOTE_PS_OBJECT);
    
}
