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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;

public class OverthereWinRMPowerShell{
    
    // The "logger" field has to be declared and defined at the top so that the static initializer below can access it
    private static final Logger logger = LoggerFactory.getLogger(OverthereWinRMPowerShell.class);

    
    
    /**
     * Creates a connection.
     * 
     * @param protocol
     *            The protocol to use, e.g. "local".
     * @param options
     *            A set of options to use for the connection.
     * @return the connection.
     */
    public static OverthereConnection getConnection(String protocol, ConnectionOptions options) {
        OverthereConnectionBuilder connectionBuilder = new CifsPowerShellConnectionBuilder(protocol, options);
        OverthereConnection connection = connectionBuilder.connect();
        logger.info("Connecting to {}", connectionBuilder);
        return connection;
    }
}
