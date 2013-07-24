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
import org.dom4j.Namespace;
import org.dom4j.XPath;
import org.jaxen.SimpleNamespaceContext;

import com.xebialabs.overthere.cifs.winrm.Namespaces;

public enum FaultExtractor {

    FAULT_CODE("WSManFault/@Code", Namespaces.NS_WSMAN_FAULT),
    FAULT_MESSAGE("Message", Namespaces.NS_WSMAN_FAULT);

    private final String expr;
    private final Namespace ns;
    private final SimpleNamespaceContext namespaceContext;

    FaultExtractor(String expr) {
        this(expr, Namespaces.NS_WIN_SHELL);
    }


    FaultExtractor(String expr, Namespace ns) {
        this.expr = expr;
        this.ns = ns;
        namespaceContext = new SimpleNamespaceContext();
        namespaceContext.addNamespace(ns.getPrefix(), ns.getURI());
    }

    public XPath getXPath() {
        final XPath xPath = DocumentHelper.createXPath("//" + ns.getPrefix() + ":" + expr);
        xPath.setNamespaceContext(namespaceContext);
        return xPath;
    }
}
