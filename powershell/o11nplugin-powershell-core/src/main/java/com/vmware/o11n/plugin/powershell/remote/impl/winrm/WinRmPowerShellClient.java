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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.remote.AuthenticationException;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.HttpConnector;
import com.xebialabs.overthere.cifs.winrm.Namespaces;
import com.xebialabs.overthere.cifs.winrm.ResourceURI;
import com.xebialabs.overthere.cifs.winrm.ResponseExtractor;
import com.xebialabs.overthere.cifs.winrm.SoapAction;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMAuthorizationException;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;

enum ClientState {
    Pending, Initialized, Done
}

public class WinRmPowerShellClient {

    private static Logger log = LoggerFactory.getLogger(WinRmPowerShellClient.class);
    private static final String CHARSET_UTF_8 = "UTF-8";

    private final URL targetURL;
    private final HttpConnector connector;

    private String timeout;
    private int envelopSize;
    private String locale;

    private String exitCode;
    private String shellId;
    private String commandId;

    private int chunk = 0;

    private ClientState state = ClientState.Pending;

    public WinRmPowerShellClient(HttpConnector connector, URL targetURL) {
        this.connector = connector;
        this.targetURL = targetURL;
    }

    synchronized public void open(String command) {
        shellId = openShell();
        if (shellId == null) {
            throw new WinRMRuntimeIOException("Unable to open winrm shell. ");
        }
        commandId = runCommand(command);
        if (commandId == null) {
            throw new WinRMRuntimeIOException("Unable to start winrm command. ");
        }

    }

    synchronized public void close() {
        log.debug("Closing WinRm client for shell {}", shellId);
        cleanUp();
        closeShell();
    }

    private void closeShell() {
        if (shellId == null)
            return;
        log.debug("closeShell shellId {}", shellId);
        final Document requestDocument = getRequestDocument(Action.WS_DELETE, ResourceURI.RESOURCE_URI_CMD, null,
                shellId, null);
        @SuppressWarnings("unused")
        Document responseDocument = sendMessage(requestDocument, null);
    }

    private void cleanUp() {
        if (commandId == null)
            return;
        log.debug("cleanUp shellId {} commandId {} ", shellId, commandId);
        final Element bodyContent = DocumentHelper.createElement(QName.get("Signal", Namespaces.NS_WIN_SHELL))
                .addAttribute("CommandId", commandId);
        bodyContent.addElement(QName.get("Code", Namespaces.NS_WIN_SHELL)).addText(
                "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");
        final Document requestDocument = getRequestDocument(Action.WS_SIGNAL, ResourceURI.RESOURCE_URI_CMD, null,
                shellId, bodyContent);
        @SuppressWarnings("unused")
        Document responseDocument = sendMessage(requestDocument, SoapAction.SIGNAL);

    }

    /**
     * Loops endlessly and provides output stream content to the OverthereProcessOutputHandler instance 
     * @param handler - instance of OverthereProcessOutputHandler that will collect the output
     */
    synchronized public void getCommandOutput(WinRmCapturingOverthereProcessOutputHandler handler) {
        log.debug("getCommandOutput shellId {} commandId {} ", shellId, commandId);
        final Element bodyContent = DocumentHelper.createElement(QName.get("Receive", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("DesiredStream", Namespaces.NS_WIN_SHELL))
                .addAttribute("CommandId", commandId).addText("stdout stderr");
        final Document requestDocument = getRequestDocument(Action.WS_RECEIVE, ResourceURI.RESOURCE_URI_CMD, null,
                shellId, bodyContent);

        for (;;) {
            Document responseDocument = sendMessage(requestDocument, SoapAction.RECEIVE);

            //  If no output is available before the wsman:OperationTimeout expires, 
            //  the server MUST return a WSManFault with the Code attribute equal to "2150858793". 
            //  When the client receives this fault, it SHOULD issue another Receive request. 
            //  The client SHOULD continue to issue Receive messages as soon as the previous ReceiveResponse has been received.
            final List<?> faults = FaultExtractor.FAULT_CODE.getXPath().selectNodes(responseDocument);
            if (!faults.isEmpty()) {
                String faultCode = ((Attribute) faults.get(0)).getText();
                final List<?> faultMessages = FaultExtractor.FAULT_MESSAGE.getXPath().selectNodes(responseDocument);
                String faultMessage = ((Element) faultMessages.get(0)).getText();
                log.debug("fault code {} message", faultCode);
                if (faultCode.equalsIgnoreCase("2150858793")) {
                    continue;
                } else {
                    log.debug("fault code {}", faultCode);
                    throw new WinRMRuntimeIOException(faultMessage);
                }
            }

            String stdout = handleStream(responseDocument, ResponseExtractor.STDOUT);
            handler.handleOutputChunk(stdout);

            String stderr = handleStream(responseDocument, ResponseExtractor.STDERR);
            BufferedReader stderrReader = new BufferedReader(new StringReader(stderr));
            try {
                for (;;) {
                    String line = stderrReader.readLine();
                    if (line == null) {
                        break;
                    }
                    handler.handleErrorLine(line);
                }
            } catch (IOException exc) {
                throw new RuntimeIOException("Unexpected I/O exception while reading stderr", exc);
            }

            if (chunk == 0) {
                try {
                    exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
                    log.debug("exit code {}", exitCode);
                } catch (Exception e) {
                    log.debug("not found");
                }
            }
            chunk++;

            /* We may need to get additional output if the stream has not finished.
                                        The CommandState will change from Running to Done like so:
                                        @example

                                         from...
                                         <rsp:CommandState CommandId="..." State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Running"/>
                                         to...
                                         <rsp:CommandState CommandId="..." State="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done">
                                             <rsp:ExitCode>0</rsp:ExitCode>
                                         </rsp:CommandState>
                                     */
            final List<?> list = ResponseExtractor.STREAM_DONE.getXPath().selectNodes(responseDocument);
            if (!list.isEmpty()) {
                exitCode = getFirstElement(responseDocument, ResponseExtractor.EXIT_CODE);
                log.debug("exit code {}", exitCode);
                break;
            }

            if (handler.isDone() == true) {
                break;
            }
        }
        //logger.debug("all the command output has been fetched (chunk={})", chunk);
    }

    private String handleStream(Document responseDocument, ResponseExtractor stream) {
        StringBuffer buffer = new StringBuffer();
        @SuppressWarnings("unchecked")
        final List<Element> streams = (List<Element>) stream.getXPath().selectNodes(responseDocument);
        if (!streams.isEmpty()) {
            final Base64 base64 = new Base64();
            Iterator<Element> itStreams = streams.iterator();
            while (itStreams.hasNext()) {
                Element e = itStreams.next();
                //TODO check performance with http://www.iharder.net/current/java/base64/
                final byte[] decode = base64.decode(e.getText().getBytes());
                buffer.append(new String(decode));
            }
        }
        log.debug("handleStream {} buffer {}", stream, buffer);
        return buffer.toString();

    }

    private String runCommand(String command) {
        log.debug("runCommand shellId {} command {}", shellId, command);
        final Element bodyContent = DocumentHelper.createElement(QName.get("CommandLine", Namespaces.NS_WIN_SHELL));

        String encoded = command;
        if (!command.startsWith("\""))
            encoded = "\"" + encoded;
        if (!command.endsWith("\""))
            encoded = encoded + "\"";

        log.debug("Encoded command is {}", encoded);

        bodyContent.addElement(QName.get("Command", Namespaces.NS_WIN_SHELL)).addText(encoded);

        final Document requestDocument = getRequestDocument(Action.WS_COMMAND, ResourceURI.RESOURCE_URI_CMD,
                OptionSet.RUN_COMMAND, shellId, bodyContent);
        Document responseDocument = sendMessage(requestDocument, SoapAction.COMMAND_LINE);

        return getFirstElement(responseDocument, ResponseExtractor.COMMAND_ID);
    }

    synchronized public void sendCmdToInputStream(String command, boolean shouldEncode) {
        log.debug("runCommand shellId {} command {}", shellId, command);
        final Element bodyContent = DocumentHelper.createElement(QName.get("Send", Namespaces.NS_WIN_SHELL));
        String encoded = null;
        if (shouldEncode) {
            try {
                encoded = new String(Base64.encodeBase64(command.getBytes()), CHARSET_UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        String cmdToSent = shouldEncode ? encoded : command;
        log.debug("Encoded command is {}", cmdToSent);

        bodyContent.addElement(QName.get("Stream", Namespaces.NS_WIN_SHELL)).addText(cmdToSent)
                .addAttribute("Name", "stdin").addAttribute("CommandId", commandId);

        final Document requestDocument = getRequestDocument(Action.WS_SEND, ResourceURI.RESOURCE_URI_CMD,
                OptionSet.RUN_COMMAND, shellId, bodyContent);
        sendMessage(requestDocument, SoapAction.COMMAND_LINE);
    }

    private String getFirstElement(Document doc, ResponseExtractor extractor) {
        @SuppressWarnings("unchecked")
        final List<Element> nodes = (List<Element>) extractor.getXPath().selectNodes(doc);
        if (nodes.isEmpty())
            throw new RuntimeException("Cannot find " + extractor.getXPath() + " in " + toString(doc));

        final Element next = (Element) nodes.iterator().next();
        return next.getText();
    }

    private String openShell() {
        log.debug("openShell");

        final Element bodyContent = DocumentHelper.createElement(QName.get("Shell", Namespaces.NS_WIN_SHELL));
        bodyContent.addElement(QName.get("InputStreams", Namespaces.NS_WIN_SHELL)).addText("stdin");
        bodyContent.addElement(QName.get("OutputStreams", Namespaces.NS_WIN_SHELL)).addText("stdout stderr");

        final Document requestDocument = getRequestDocument(Action.WS_ACTION, ResourceURI.RESOURCE_URI_CMD,
                OptionSet.OPEN_SHELL, null, bodyContent);
        Document responseDocument = sendMessage(requestDocument, SoapAction.SHELL);

        return getFirstElement(responseDocument, ResponseExtractor.SHELL_ID);

    }

    private Document sendMessage(Document requestDocument, SoapAction soapAction) {
        try { 
            return connector.sendMessage(requestDocument, soapAction);
        } catch (WinRMAuthorizationException e){
            throw new AuthenticationException(e.getMessage(), e);
        } catch (WinRMRuntimeIOException e){
            if ( e.getCause() instanceof AuthenticationException){
                log.error(e.getMessage());
                throw (AuthenticationException) e.getCause();
            } 
            throw e;
        }
        
    }

    private Document getRequestDocument(Action action, ResourceURI resourceURI, OptionSet optionSet, String shelId,
            Element bodyContent) {
        Document doc = DocumentHelper.createDocument();
        final Element envelope = doc.addElement(QName.get("Envelope", Namespaces.NS_SOAP_ENV));
        envelope.add(getHeader(action, resourceURI, optionSet, shelId));

        final Element body = envelope.addElement(QName.get("Body", Namespaces.NS_SOAP_ENV));

        if (bodyContent != null)
            body.add(bodyContent);

        return doc;
    }

    private Element getHeader(Action action, ResourceURI resourceURI, OptionSet optionSet, String shellId) {
        final Element header = DocumentHelper.createElement(QName.get("Header", Namespaces.NS_SOAP_ENV));
        header.addElement(QName.get("To", Namespaces.NS_ADDRESSING)).addText(targetURL.toString());
        final Element replyTo = header.addElement(QName.get("ReplyTo", Namespaces.NS_ADDRESSING));
        replyTo.addElement(QName.get("Address", Namespaces.NS_ADDRESSING)).addAttribute("mustUnderstand", "true")
                .addText("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");
        header.addElement(QName.get("MaxEnvelopeSize", Namespaces.NS_WSMAN_DMTF))
                .addAttribute("mustUnderstand", "true").addText("" + envelopSize);
        header.addElement(QName.get("MessageID", Namespaces.NS_ADDRESSING)).addText(getUUID());
        header.addElement(QName.get("Locale", Namespaces.NS_WSMAN_DMTF)).addAttribute("mustUnderstand", "false")
                .addAttribute("xml:lang", locale);
        header.addElement(QName.get("DataLocale", Namespaces.NS_WSMAN_MSFT)).addAttribute("mustUnderstand", "false")
                .addAttribute("xml:lang", locale);
        header.addElement(QName.get("OperationTimeout", Namespaces.NS_WSMAN_DMTF)).addText(timeout);
        header.add(action.getElement());
        if (shellId != null) {
            header.addElement(QName.get("SelectorSet", Namespaces.NS_WSMAN_DMTF))
                    .addElement(QName.get("Selector", Namespaces.NS_WSMAN_DMTF)).addAttribute("Name", "ShellId")
                    .addText(shellId);
        }
        header.add(resourceURI.getElement());
        if (optionSet != null) {
            header.add(optionSet.getElement());
        }

        return header;
    }

    private String toString(Document doc) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(stringWriter, OutputFormat.createPrettyPrint());
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new WinRMRuntimeIOException("error ", e);
        }
        return stringWriter.toString();
    }

    private String getUUID() {
        return "uuid:" + java.util.UUID.randomUUID().toString().toUpperCase();
    }

    public long getExitCode() {
        return Long.parseLong(exitCode);
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getEnvelopSize() {
        return envelopSize;
    }

    public void setEnvelopSize(int envelopSize) {
        this.envelopSize = envelopSize;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public URL getTargetURL() {
        return targetURL;
    }

    synchronized public void setState(ClientState state) {
        this.state = state;
    }

    synchronized public ClientState getState() {
        return state;
    }

}
