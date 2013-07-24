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

package com.vmware.o11n.plugin.powershell.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.model.result.PSObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

enum RemotePsType{
    PrimitiveType,
    Object,
    List,
    Dictionary, 
    Ref,
    Unknown
} 

@VsoObject(description = "Wrapper for the result from an execution fo PowerShell script")
@VsoFinder(name = Constants.FINDER_POWER_SHELL_REMOTE_PS_OBJECT, idAccessor = "getId()")
public class RemotePSObject implements Serializable {

    private static final String ELEMENT_QUE = "QUE";

    private static final String ELEMENT_STK = "STK";

    private static final String ELEMENT_LST = "LST";

    private static final String ELEMENT_DCT = "DCT";

    private static final String ELEMENT_EN = "En";

    private static final String ELEMENT_OBJ = "Obj";

    private static final String ELEMENT_REF = "Ref";

    private static final String ATTR_REF_ID = "RefId";
    
    

    static final Logger log = LoggerFactory.getLogger(RemotePSObject.class);

    private static final String UTF_8 = "UTF-8";

    /**
      * 
      */
    private static final long serialVersionUID = -1603084697693407027L;
    
    private transient static XStream xStream;

    //XPath init
    private transient XPathFactory xpathFactory;

    private transient Document doc;

    private transient String refId;

    private String xml;

    private transient Map<String, Object> objects = new HashMap<String, Object>();

    private String sesionId;

//    public RemotePSObject() {
//        initXStream();
//    }

    public RemotePSObject(String xml, String sesionId) {
        this.xml = xml;
        this.sesionId = sesionId;
        initXStream();
    }

    /**
     *  The result form PowerShell execution deserialized in objects of type PSObjectList, PSObject or simple type      
     */
    @VsoMethod(description = "Returns result of PowerShell script invocation converted in corresponding vCO type. The result can be simple type, ArrayList, Properties or PowerShellPSObject")
    public Object getRootObject() {
        initDomDocument();

        if (doc == null ){
            return null;
        }
        
        NodeList nodes = doc.getDocumentElement().getChildNodes();
        
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.TEXT_NODE){
                continue;
            }
            //The result is always wrapped in single element.
            // so even if we have array of items, they will be wrapped in <LST> element
            return read(node);
        }

        return null;
    }

    /**
     * Initialize XStream.
     */
    private static synchronized void initXStream() {
        if (xStream != null) {
            return;
        }
        xStream = new XStream();
        xStream.setClassLoader(RemotePSObject.class.getClassLoader());
    }

    public void load(String stream) {
        xml = stream;
    }

    private void initDomDocument() {
        if (doc == null && xml != null) {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            //            domFactory.setNamespaceAware(true); // never forget this!
            DocumentBuilder builder;
            try {
                builder = domFactory.newDocumentBuilder();
                doc = builder.parse(IOUtils.toInputStream(xml));
            } catch (ParserConfigurationException e) {
                throw new PowerShellException(e.getMessage(), e);
            } catch (SAXException e) {
                throw new PowerShellException(e.getMessage(), e);
            } catch (IOException e) {
                throw new PowerShellException(e.getMessage(), e);
            }
            
            xpathFactory = XPathFactory.newInstance();
        }

    }

    @VsoMethod(description="Returns the object as String that is XML formated")
    public String getXml() {
        return xml;
    }

    @VsoMethod(description="Returns the object as String that is JSON formated")
    public String getAsJson() {
        if  ( xml == null ){
            return null;
        }
        
        try {
            return XML.toJSONObject(xml).toString();
        } catch (JSONException e) {
            throw new PowerShellException("Cannot convert invalid xml string to JSON: " + e.getMessage(), e);
        }

    }
    

    /**
     * Serialize the object's content as Base64 encoded string.
     *
     * @return the the serialized content of the object as an UTF-8 string
     */
    public String getId() {
        if (refId == null )
        {
            return null;
        }
        return  sesionId + "@" + refId;
    }
//
//    public static RemotePSObject deseriallize(final String content) {
//        Validate.notEmpty(content, "The content cannot be null!");
//        initXStream();
//        byte[] decode = Base64.decode(content.toCharArray());
//        RemotePSObject result;
//
//        try {
//            result = (RemotePSObject) xStream.fromXML(new String(decode, UTF_8));
//        } catch (UnsupportedEncodingException e) {
//            throw new PowerShellException(e);
//        }
//
//        return result;
//    }

    public Object read(Node node){
        Object res = "";
        RemotePsType type = getType(node);
        if( type == RemotePsType.PrimitiveType ) {
            res = convertPrimitiveType(node.getNodeName(), node.getTextContent());
        } else if (type == RemotePsType.List ) {
            res = readList(node);
        } else if (type == RemotePsType.Dictionary ){
            res = readDictionary(node);
        } else if( type == RemotePsType.Object ) {
            res = readObject(node);
        } else if ( type == RemotePsType.Ref){
            String refId = node.getAttributes().getNamedItem(ATTR_REF_ID).getNodeValue();
            return getObjByRefId(refId);
        }else {
            throw new  RuntimeException("Unsupported type.");
        }
        
        return res;
    }

    private Object readDictionary(Node node) {

        Hashtable<Object,Object> res = new Hashtable<Object,Object>();
        List<Node> dicts = getElementsByName(node, ELEMENT_DCT);
        for ( Node dict : dicts ) {
            List<Node> entries = getElementsByName(dict, ELEMENT_EN);
            for ( Node entry : entries ) {
                Node keyNode = getChildByNameAttr(entry, "Key");
                Node valueNode = getChildByNameAttr(entry, "Value");
                if (keyNode != null && valueNode != null) {
                    Object key = read(keyNode);
                    Object value = read(valueNode);
                    res.put(key, value);
                } else {
                    log.warn("Invalid dictionary entry found in object RefId = " + node.getAttributes().getNamedItem(ATTR_REF_ID).getTextContent());
                }
            }
        }

        String refId = node.getAttributes().getNamedItem(ATTR_REF_ID).getNodeValue();
        registerObject(refId, res);
        return res;
    }

    /**
     * Returns list of elements with provided name.
     * @param node
     * @param name
     * @return
     */
    private static List<Node> getElementsByName(final Node node, final String name) {
        List<Node> result = new ArrayList<Node>();
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node enNode = childs.item(i);
            if (enNode.getNodeType() == Node.ELEMENT_NODE && (enNode.getNodeName().equals(name))) {
                result.add(enNode);
            }
        }

        return result;
    }

    /**
     * Returns child element containing attribute 'N' maching provided name. 
     * @param node Child nodes to be searched
     * @param name Search name
     * @return maching element.
     */
    private static Node getChildByNameAttr(final Node node, final String name ){; 
        NodeList childs = node.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Node nodeName = child.getAttributes().getNamedItem("N");
                if (nodeName != null && nodeName.getTextContent().equals(name) ){
                    return child;
                }
            }
        }
        
        return null;
    }

    private Object readObject(Node node) {
        Object res;
        String refId = node.getAttributes().getNamedItem(ATTR_REF_ID).getNodeValue();
        res = new PSObject(node, this);
        registerObject(refId, res);
        return res;
    }

    private Object readList(Node node) {
        
        List<Object> res = new ArrayList<Object>();
        NodeList childs = node.getChildNodes();
        for( int i=0; i< childs.getLength(); i++){
            Node listNode  = childs.item(i);
            if (listNode.getNodeType() == Node.ELEMENT_NODE
                    && (listNode.getNodeName().equals(ELEMENT_LST) || listNode.getNodeName().equals(ELEMENT_STK) || listNode
                            .getNodeName().equals(ELEMENT_QUE))) {
                NodeList listElements = listNode.getChildNodes();
                for (int j = 0; j < listElements.getLength(); j++) {
                    if (listElements.item(j).getNodeType() == Node.ELEMENT_NODE) {
                        res.add(read(listElements.item(j)));
                    }
                }
            }
        }            
        
        String refId = node.getAttributes().getNamedItem(ATTR_REF_ID).getNodeValue();
        List<?> list = Collections.unmodifiableList(res);
        registerObject(refId, list);
        return list;
    }
    
    private RemotePsType getType(Node node) {
        if (isPrimitiveType(node)) {
            return RemotePsType.PrimitiveType;
        } else if (node.getNodeName().equals(ELEMENT_REF)) {
            return RemotePsType.Ref;
        } else if (node.getNodeName().equals(ELEMENT_OBJ)) {
            //check if this is known container
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    if (item.getNodeName().equals(ELEMENT_LST) || item.getNodeName().equals(ELEMENT_STK)
                            || item.getNodeName().equals(ELEMENT_QUE)) {
                        return RemotePsType.List;
                    } else if (item.getNodeName().equals(ELEMENT_DCT)) {
                        return RemotePsType.Dictionary;
                    }
                }
            }

            return RemotePsType.Object;
        }

        return RemotePsType.Unknown;
    }

    private static final HashSet<String> simpleTypes = new HashSet<String>();
    static {
        simpleTypes.add("S");       simpleTypes.add("C");       simpleTypes.add("B");        simpleTypes.add("DT");
        simpleTypes.add("TS");      simpleTypes.add("By");      simpleTypes.add("SB");       simpleTypes.add("U16");
        simpleTypes.add("I16");     simpleTypes.add("U32");     simpleTypes.add("I32");      simpleTypes.add("U64");
        simpleTypes.add("I64");     simpleTypes.add("Sg");      simpleTypes.add("Db");       simpleTypes.add("D");
        simpleTypes.add("BA");      simpleTypes.add("G");       simpleTypes.add("URI");      simpleTypes.add("Nil");
        simpleTypes.add("Version"); simpleTypes.add("XD");      simpleTypes.add("SBK");      simpleTypes.add("SS");
        simpleTypes.add("PR");
        
    }

    private static boolean isPrimitiveType(Node node) {
        return simpleTypes.contains(node.getNodeName());
    }

    // Need conversion for :    
    //  DateTime, Timestamp, UnsignedLong, Decimal
    //  ArrayOfBytes("BA"), GUID("G"), URI("URI"), Null("Nil"),
    //  Version("Version"), XMLDocument("XD"), ScriptBlock("SBK"), SecureString("SS"),
    //  ProgressRecord("PR")    
    private Object convertPrimitiveType(String type, String value) {
        if (type.equals("C")) { //Char
            return (char) Integer.valueOf(value).intValue();
        } if (type.equals("B")) {  //Boolean
            return Boolean.valueOf(value);
        } if (type.equals("By")) { //UnsignedByte
            return Short.valueOf(value); 
        } if (type.equals("SB")) { //SignedByte
            return Byte.valueOf(value); 
        } if (type.equals("U16")) { //UnsignedShort
            return Integer.valueOf(value); 
        } if (type.equals("I16")) { //SignedShort
            return Short.valueOf(value); 
        } if (type.equals("U32")) { //UnsignedInt
            return Long.valueOf(value); 
        } if (type.equals("I32")) { //SignedInt
            return Integer.valueOf(value); 
        } if (type.equals("I64")) { //SignedLong
            return Long.valueOf(value); 
        } if (type.equals("Sg")) { //Float
            return Float.valueOf(value); 
        } if (type.equals("Db")) { //Double
            return Double.valueOf(value); 
        } else {
            return value;    
        }
        
        
    }

    
    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getRefId() {
        return refId;
    }

    private Object getObjByRefId(String refId) {
        Object obj = objects.get(refId);
        if (obj != null ){
            return obj;
        }
        
        try {
            //TODO precompile
            XPath xpathFindByRefId = xpathFactory.newXPath();
            Node propNode = (Node) xpathFindByRefId.evaluate(String.format("//Obj[@RefId=\"%s\"]", refId), doc.getDocumentElement(), XPathConstants.NODE);
            obj =  read(propNode);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Unable to resolve object with id " + refId);
        }
        
        return obj;
    }

    private void registerObject(String refId, Object obj) {
        log.trace("Registering object. RefId = " + refId);
        objects.put(refId, obj);
    }
    
    public String getSessionId(){
        return sesionId;        
    }

}
