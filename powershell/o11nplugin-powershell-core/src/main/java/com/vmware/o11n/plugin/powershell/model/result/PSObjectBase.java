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

package com.vmware.o11n.plugin.powershell.model.result;

import org.w3c.dom.Node;

import com.vmware.o11n.plugin.powershell.model.RemotePSObject;

public abstract class PSObjectBase {
    protected Node node;

    protected RemotePSObject context = null;

    public PSObjectBase(Node xmlNode, RemotePSObject context){
        this.node = xmlNode;
        this.context = context;
    }    
    
    protected boolean isReference(Node propNode) {
        return propNode.getNodeName().equalsIgnoreCase("Ref");
    }

    protected boolean isTypeReference(Node propNode) {
        return propNode.getNodeName().equalsIgnoreCase("TNRef");
    }


}
