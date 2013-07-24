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
package com.vmware.o11n.plugin.powershell.scripting;

import com.vmware.o11n.plugin.sdk.spring.MultipartId;

public class PowerShellBaseObject {

    private String name;
    private MultipartId id;
    private PowerShellHost host;

    protected PowerShellBaseObject(PowerShellHost host, String name, MultipartId id) {
        this.host = host;
        this.name = name;
        this.id= id;
    }

    protected PowerShellBaseObject(PowerShellHost host, String name) {
        this.host = host;
        this.name = name;
        this.id = MultipartId.random();                
    }

    String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id.toString();
    }

    public void setId(MultipartId id) {
        this.id = id;
    }

    public PowerShellHost getHost() {
        return host;
    }

}
