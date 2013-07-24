/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */

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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import com.xebialabs.overthere.cifs.winrm.Namespaces;

enum Action {

    WS_ACTION("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create"),
    WS_COMMAND("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command"),
    WS_SEND("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Send"),
    WS_RECEIVE("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive"),
    WS_SIGNAL("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal"),
    WS_DELETE("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete");

    private String uri;

    Action(String uri) {
        this.uri = uri;
    }

    public Element getElement() {
        return DocumentHelper.createElement(QName.get("Action", Namespaces.NS_ADDRESSING)).addAttribute("mustUnderstand", "true").addText(uri);
    }

}

