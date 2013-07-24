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

import java.util.Hashtable;

import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
import com.vmware.o11n.plugin.sdk.ssl.HostValidator;

@VsoObject(description="Validates URLs.", create = false, strict = true)
public class PowerShellHostValidator extends HostValidator {

    private static final long serialVersionUID = 1L;

    @VsoConstructor
    public PowerShellHostValidator( @VsoParam(name="url", description="URL to validate") String url) {
        super(url);
    }

    @Override
    @VsoMethod(description="Retrieves the server's certificate info as a string.")
    public Hashtable<String, Object> getCertificateInfo() throws Exception {
        return super.getCertificateInfo();
    }

    @Override
    @VsoMethod(description="Installs the server's certificate into the JSSE keystore (only the server's specific certificate, not the whole chain).")
    public void installCertificates() throws Exception {
        super.installCertificates();
    }
}
