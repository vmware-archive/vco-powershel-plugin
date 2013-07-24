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

package com.vmware.o11n.plugin.powershell.config;

import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * @deprecated Typo in the class name was fixed, replaced by {@link #AuthorizationMode}
 */
@Deprecated
@VsoObject(description="Deprecated. Use AuthorizationModeinstead.<br/>Enumeration for the supported authorization modes. The modes are \"Shared Session\" and \"Session per User\". ")
public enum AutorizationMode {
    
    Shared("Shared Session"), 
    PerUser("Session per User");

    String caption;

    private AutorizationMode(String caption) {
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }
    
    @Deprecated
    @VsoMethod(description="Converts a string to an instance of this class. If String is not possible to be converted an IllegalArgumentException is thrown.")
    public static AutorizationMode fromString(String text) {
        if (text != null) {
            for (AutorizationMode b : AutorizationMode.values()) {
                if (text.equalsIgnoreCase(b.caption)) {
                    return b;
                }
            }
        }
        throw new IllegalArgumentException("Invalid authorization mode");
    }
    
    @VsoMethod(description="Converts a string to an instance of AuthorizationMode class. If String is not possible to be converted an IllegalArgumentException is thrown.")
    public static AuthorizationMode getAuthorizationMode(AutorizationMode mode) {
        if (mode != null) {
            for (AuthorizationMode b : AuthorizationMode.values()) {
                if (mode.caption.equalsIgnoreCase(b.caption)) {
                    return b;
                }
            }
        }
        throw new IllegalArgumentException("Invalid authorization mode");
    }    
}
    
