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

import java.util.ArrayList;
import java.util.List;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.model.SnapInInfo;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;
import com.vmware.o11n.plugin.sdk.spring.MultipartId;

@VsoObject(create = false, description="The root of the SnapIns")
@VsoFinder(name = Constants.FINDER_POWER_SHELL_SNAPIN_ROOT, idAccessor = "getId()", 
        image = "images/snapin-32x32.PNG", relations = @VsoRelation(inventoryChildren = true,
                name = Constants.RELATION_SNAPINROOT_HAS_SNAPIN, type = Constants.FINDER_POWER_SHELL_SNAPIN))
public class PowerShellSnapInRoot extends PowerShellBaseObject {

    public static final String SNAP_INS = "SnapIns";
    
    public PowerShellSnapInRoot(PowerShellHost host) {
        super(host, SNAP_INS, new MultipartId(host.getId()));
    }
    
    @VsoProperty(displayName = "Name", readOnly = true)
    @Override
    public String getName() {
        return SNAP_INS;
    }
    
    //TODO : Add caching
    public List<PowerShellSnapIn> getSnapIns() {
        List<PowerShellSnapIn> snapins = new ArrayList<PowerShellSnapIn>();
        for (SnapInInfo info : getHost().getSnapIns()) {
            snapins.add(new PowerShellSnapIn(info, getHost()));
        }
        return snapins;
    }

    //TODO : Add caching
    public PowerShellSnapIn getSnapIn(String name) {
        SnapInInfo info = getHost().getSnapIn(name);
        if (info != null )
        {
            return new PowerShellSnapIn(info, getHost());
        }
        
        return null;
    }

    
}
