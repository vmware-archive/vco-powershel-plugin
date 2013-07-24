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
package com.vmware.o11n.plugin.powershell.remote.impl.winrm;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * Builds CIFS connections for PowerShell.
 */
@Protocol(name = "CifsWinRmPowerShell")
public class CifsPowerShellConnectionBuilder implements OverthereConnectionBuilder {

    /**
     * Name of the {@link ConnectionOptions connection option} used to specify the {@link CifsConnectionType CIFS connection type} to use.
     */
    public static final String CONNECTION_TYPE = "connectionType";
    
    /**
     * Default port (23) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#TELNET TELNET}.
     */
    public static final int DEFAULT_TELNET_PORT = 23;

    /**
     * Default port (5985) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#WINRM_HTTP WINRM_HTTP}.
     */
    public static final int DEFAULT_WINRM_HTTP_PORT = 5985;

    /**
     * Default port (5986) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#WINRM_HTTPS WINRM_HTTPS}.
     */
    public static final int DEFAULT_WINRM_HTTPS_PORT = 5986;

    /**
     * Name of the {@link ConnectionOptions connection option} used to specify the CIFS port to connect to.
     */
    public static final String CIFS_PORT = "cifsPort";

    /**
     * Default value (445) for the {@link ConnectionOptions connection option} used to specify the CIFS port to connect to.
     */
    public static final int DEFAULT_CIFS_PORT = 445;

    /**
     * Name of the {@link ConnectionOptions connection option} used to specify the context (URI) used by WinRM.
     */ 
    public static final String CONTEXT = "winrmContext";

    /**
     * Default value (/wsman) of the {@link ConnectionOptions connection option} used to specify the context (URI) used by WinRM.
     */ 
    public static final String DEFAULT_WINRM_CONTEXT = "/wsman";

    public static final String TIMEMOUT = "winrmTimeout";
    public static final String DEFAULT_TIMEOUT = "PT180.000S";
    // FIXME: Figure out what format this is

    public static final String ENVELOP_SIZE = "winrmEnvelopSize";
    public static final int DEFAULT_ENVELOP_SIZE = 153600;

    public static final String LOCALE = "winrmLocale";
    public static final String DEFAULT_LOCALE = "en-US";

    private OverthereConnection connection;

    public CifsPowerShellConnectionBuilder(String type, ConnectionOptions options) {
        CifsConnectionType cifsConnectionType = options.get(CONNECTION_TYPE);

        switch(cifsConnectionType) {
        case TELNET:
            connection = new CifsTelnetConnection(type, options);
            break;
        case WINRM_HTTP:
        case WINRM_HTTPS:
            connection = new CifsWinRmPowerShellConnection(type, options);
            break;
        default:
            throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);
        }
    }

    @Override
    public OverthereConnection connect() {
        return connection;
    }

    public String toString() {
        return connection.toString();
    }

}
