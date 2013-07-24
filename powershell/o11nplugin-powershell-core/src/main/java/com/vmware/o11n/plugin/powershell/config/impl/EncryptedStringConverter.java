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

package com.vmware.o11n.plugin.powershell.config.impl;

import ch.dunes.util.EncryptHelper;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class EncryptedStringConverter implements SingleValueConverter {
    @Override
    public boolean canConvert(Class type) {
        return type == String.class;
    }

    @Override
    public String toString(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return EncryptHelper.encrypt(obj.toString());
        }
    }

    @Override
    public Object fromString(String str) {
        if (str == null) {
            return null;
        } else {
            return EncryptHelper.decrypt(str);
        }
    }
}
