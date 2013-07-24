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

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.model.CmdletInfo;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.spring.MultipartId;

@VsoObject(description="Wrapper of a PowerShell cmdlet that resides on the remote Windows machine",create = false)
@VsoFinder(name = Constants.FINDER_POWER_SHELL_CMDLET, idAccessor = "getId()", image = "images/cmdlet-32x32.PNG")
public class PowerShellCmdlet extends PowerShellBaseObject{

    private CmdletInfo info;

    public PowerShellCmdlet(CmdletInfo info, PowerShellHost host) {
        super(host, info.getName(), new MultipartId(host.getId()).with(info.getPsSnapin()).with(info.getName()));
        this.info = info;
    }

    @Override
    @VsoProperty(displayName = "Name", description="The name of the cmdlet", readOnly = true)
    public String getName() {
        return getInfo().getName();
    }

    @VsoProperty(displayName = "Definition", description="The definition of the cmdlet", readOnly = true)
    public String getDefinition() {
        return getInfo().getDefinition();
    }

    @VsoProperty(displayName = "PSSnapin", description="The name of snap-in that this cmdlet belongs to", readOnly = true)
    public String getPsSnapin() {
        return getInfo().getPsSnapin();
    }

    @VsoProperty(displayName = "CommandType", description="The cmdlet command type", readOnly = true)
    public String getCommandType() {
        return getInfo().getCommandType();
    }

    public CmdletInfo getInfo() {
        return info;
    }
    
}
