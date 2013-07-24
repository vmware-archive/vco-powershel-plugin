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
import com.vmware.o11n.plugin.powershell.model.SnapInInfo;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;
import com.vmware.o11n.plugin.sdk.spring.MultipartId;

@VsoObject(create = false, description="PowerShell SnapIn wrapper object")
@VsoFinder(name = Constants.FINDER_POWER_SHELL_SNAPIN, idAccessor = "getId()", image = "images/snapin-32x32.PNG",
        relations = @VsoRelation(inventoryChildren = true, name = Constants.RELATION_SNAPIN_HAS_CMDLET,
                type = Constants.FINDER_POWER_SHELL_CMDLET))
public class PowerShellSnapIn extends PowerShellBaseObject {

    private SnapInInfo info;
    public PowerShellSnapIn(SnapInInfo info, PowerShellHost host) {
        super(host, info.getName(), new MultipartId(host.getId()).with(info.getName()));
        this.info = info;
    }

    @Override
    @VsoProperty(displayName = "Name", readOnly = true, description="The name of the SnapIn")
    public String getName() {
        return info.getName();
    }

    @VsoProperty(displayName = "ModuleName", readOnly = true, description="The module name that this SnapIn belongs to")
    public String getModuleName() {
        return info.getModuleName();
    }

    @VsoProperty(displayName = "Version", readOnly = true, description="The version of the SnapIn")
    public String getVersion() {
        return info.getVersion();
    }

    @VsoProperty(displayName = "Description", readOnly = true, description="The description of the SnapIn")
    public String getDescription() {
        return info.getDescription();
    }
/*    
    public PowerShellCmdlet getCmdletInfo(String cmdletName ) {
        CmdletInfo info = getHost().getCmdletInfo(cmdletName, getName());
        PowerShellCmdlet cmdlet = null;
        if (info!= null) {
            return new PowerShellCmdlet(info, getHost());
        }

        return cmdlet;
    }
    public List<PowerShellCmdlet> getCmdletInfo() {
        List<CmdletInfo> cmdletInfos = getHost().getCmdletInfo(getName());
        List<PowerShellCmdlet> cmdlets = new ArrayList<PowerShellCmdlet>();
        for (CmdletInfo info : cmdletInfos) {
            cmdlets.add(new PowerShellCmdlet(info, getHost()) );
        }

        return cmdlets;
    }
*/

    
}
