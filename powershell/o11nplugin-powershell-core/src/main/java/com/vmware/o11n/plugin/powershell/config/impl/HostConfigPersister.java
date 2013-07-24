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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.dunes.model.resource.IResourceElementFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JReader;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.vmware.o11n.plugin.powershell.config.PowerShellHostConfig;
import com.vmware.o11n.plugin.sdk.spring.platform.AbstractResourceElementConfigPersister;

public final class HostConfigPersister extends AbstractResourceElementConfigPersister<PowerShellHostConfig> {

    public HostConfigPersister(IResourceElementFactory factory) {
        super(factory);
    }

    private XStream newXStream() {
        XStream xstream = new XStream();
        xstream.alias("host", PowerShellHostConfig.class);
        xstream.registerLocalConverter(PowerShellHostConfig.class, "password", new EncryptedStringConverter());
        return xstream;
    }

    @Override
    protected byte[] config2Bytes(PowerShellHostConfig config) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(baos, "UTF-8");
        newXStream().toXML(config, writer);
        writer.flush();
        return baos.toByteArray();
    }

    private byte[] convertOldFormat(byte[] bytes) {
        String XSLT =     "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
                        + "  <xsl:template match=\"/\">" + "    <xsl:apply-templates select=\"*\"/>" + "  </xsl:template>"
                        + "  <xsl:template match=\"node()\">"
                        + "    <xsl:copy><xsl:apply-templates select=\"node()\"/></xsl:copy>" + "  </xsl:template>"
                        + "  <xsl:template match=\"autorizationMode\">"
                        + "    <authorizationMode><xsl:apply-templates select=\"node()\"/></authorizationMode>"
                        + "  </xsl:template> " 
                        + "</xsl:stylesheet>";
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(XSLT)));
            Source xmlSource = new StreamSource(bais);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult out = new StreamResult(baos);
            transformer.transform(xmlSource, out);
            return baos.toByteArray();
        } catch (TransformerException e) {
            throw new RuntimeException("Unable to convert configuration.", e);
        }
    }

    @Override
	protected PowerShellHostConfig bytes2Config(byte[] bytes, String configName)
			throws IOException {
        
        byte[] cfgConverted = convertOldFormat(bytes);
		ByteArrayInputStream bais = new ByteArrayInputStream(cfgConverted);
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 	    
//	    factory.setNamespaceAware(true); 
//	    DocumentBuilder builder = factory.newDocumentBuilder(); 
//	 
//	    Document doc  = builder.parse(bais); 
//		NodeList elementsWithTypo = doc.getElementsByTagName("autorizationMode");
//		for (int index = 0; index < elementsWithTypo.getLength(); index++){
//		    Node element = elementsWithTypo.item(index);
//		    Node parent = element.getParentNode();
//		    Element newNode = doc.createElement("authorizationMode");
//		    newNode.
//		    parent.replaceChild(., element);
//		}
		Reader reader = new InputStreamReader(bais, "UTF-8");
		return (PowerShellHostConfig) newXStream().fromXML(reader);
	}

    @Override
    public String[] getPath() {
        return new String[] { "Library", "PowerShell" };
    }
}
