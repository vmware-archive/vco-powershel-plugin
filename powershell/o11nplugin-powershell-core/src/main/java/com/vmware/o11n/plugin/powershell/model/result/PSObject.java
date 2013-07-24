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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vmware.o11n.plugin.powershell.model.RemotePSObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

@VsoObject(create = false, description="Represents an object on the remote PowerShell machine")
public class PSObject extends PSObjectBase {

    private static final String ELEM_TO_STRING = "ToString";
    private static final String ELEM_MS = "MS";
    private static final String ELEM_PROPS = "Props";

    Map<String, Object> properties = null;
    String toString = null;

    public PSObject(Node xmlNode, RemotePSObject context) {
        super(xmlNode, context);

    }

    @VsoMethod(description="Checks if this instance is of specific type")
    public boolean instanceOf(@VsoParam(description="The full name of the PowerShell Type") String type) {
        List<String> types = getTypes();
        return types.contains(type);
    }

    @VsoMethod(description="Returns an array containing Types representing all the public classes and interfaces that object implements")
    public List<String> getTypes() {

        ArrayList<String> res = new ArrayList<String>();

        XPath xpath = XPathFactory.newInstance().newXPath();

        try {
            Node typeNode = (Node) xpath.evaluate(String.format("./TN | ./TNRef"), this.node, XPathConstants.NODE);
            if (isTypeReference(typeNode)) {
                String refId = typeNode.getAttributes().getNamedItem("RefId").getNodeValue();
                typeNode = (Node) xpath.evaluate(String.format("//TN[@RefId=\"%s\"]", refId), this.node
                        .getOwnerDocument().getDocumentElement(), XPathConstants.NODE);
            }

            NodeList typeNodes = typeNode.getChildNodes();
            for (int i = 0; i < typeNodes.getLength(); i++) {
                String textContent = typeNodes.item(i).getTextContent();
                if (textContent != null && textContent.trim().length() > 0) {
                    res.add(textContent);
                }
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Unable to resolve object types.");
        }

        return res;
    }

    @VsoMethod(description = "Returns value of specific object property. Tha returned value itself can be of primitive type, ArrayList, Hashtable or another PowerShellPSObject")
    public Object getProperty(String propName) {

        ensureLoaded();
        return properties.get(propName);
    }

    private void ensureLoaded() {
        if (properties != null) {
            return;
        }

        properties = new HashMap<String, Object>();

        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node curEl = childs.item(i);
            if (curEl.getNodeType() == Node.ELEMENT_NODE) {
                if (curEl.getNodeName().equals(ELEM_PROPS) || curEl.getNodeName().equals(ELEM_MS)) {
                    readProperties(curEl);
                } else if (curEl.getNodeName().equals(ELEM_TO_STRING)) {
                    this.toString = curEl.getTextContent();
                }
            }
        }
    }

    private void readProperties(Node curEl) {
        NodeList propNodes = curEl.getChildNodes();
        for (int j = 0; j < propNodes.getLength(); j++) {
            Node propNode = propNodes.item(j);
            if (propNode.getNodeType() == Node.ELEMENT_NODE) {
                Object obj = context.read(propNode);
                properties.put(propNode.getAttributes().getNamedItem("N").getNodeValue(), obj);
            }
        }
    }

    @VsoMethod(description="Returns the object converted to String.")
    public String getToString() {
        ensureLoaded();
        return toString;
    }

    @VsoMethod (description = "Returns calue of the provided property converting it to string.")
    public String getPropertyAsString(String propName) {
        Object prop = getProperty(propName);
        if (prop != null) {
            return prop.toString();
        }

        return null;
    }

    @VsoMethod (description = "Returns the value of propName property.")
    public PSObject getPropertyAsPSObject(String propName) {
        return (PSObject) getProperty(propName);
    }

    @SuppressWarnings("unchecked")
    @VsoMethod (description = "Returns the value of propName property as a list of PowerShellPSObject")
    public List<Object> getPropertyAsPSObjectList(String propName) {
        return (List<Object>) getProperty(propName);
    }

}
