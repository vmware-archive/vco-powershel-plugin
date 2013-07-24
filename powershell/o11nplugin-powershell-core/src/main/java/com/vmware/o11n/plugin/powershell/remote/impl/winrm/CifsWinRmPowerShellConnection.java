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

import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.CONTEXT;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.DEFAULT_ENVELOP_SIZE;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.DEFAULT_LOCALE;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.DEFAULT_TIMEOUT;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.ENVELOP_SIZE;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.LOCALE;
import static com.vmware.o11n.plugin.powershell.remote.impl.winrm.CifsPowerShellConnectionBuilder.TIMEMOUT;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;

import com.vmware.o11n.plugin.powershell.PowerShellPluginConfiguration;
import com.vmware.o11n.plugin.powershell.scripting.PowerShellCmdlet;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection;
import com.xebialabs.overthere.cifs.winrm.HttpConnector;
import com.xebialabs.overthere.cifs.winrm.TokenGenerator;
import com.xebialabs.overthere.cifs.winrm.connector.JdkHttpConnector;
import com.xebialabs.overthere.cifs.winrm.connector.LaxJdkHttpConnector;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.tokengenerator.BasicTokenGenerator;

/**
 * A connection to a remote host using CIFS and WinRM.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator access is needed.</li>
 * <li>Can only authenticate with basic authentication to WinRM</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsWinRmPowerShellConnection extends CifsConnection {
    public static final String AUTHENTICATION_SCHEMA = "authenticationSchema";
    public static final String AUTHENTICATION_SCHEMA_BASIC = "Basic";
    public static final String AUTHENTICATION_SCHEMA_KERBEROS = "Kerberos";

    private final WinRmPowerShellClient winRmClient;

    /**
     * Creates a {@link CifsWinRmConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsWinRmPowerShellConnection(String type, ConnectionOptions options) {
        super(type, options, false);

        TokenGenerator tokenGenerator = getTokenGenerator(options);
        URL targetURL = getTargetURL(options);
        HttpConnector httpConnector = newHttpConnector(cifsConnectionType, targetURL, tokenGenerator);

        winRmClient = new WinRmPowerShellClient(httpConnector, targetURL);
        winRmClient.setTimeout(options.get(TIMEMOUT, DEFAULT_TIMEOUT));
        winRmClient.setEnvelopSize(options.get(ENVELOP_SIZE, DEFAULT_ENVELOP_SIZE));
        winRmClient.setLocale(options.get(LOCALE, DEFAULT_LOCALE));
    }

    private TokenGenerator getTokenGenerator(ConnectionOptions options) {
        String username = options.get(USERNAME);
        String password = options.get(PASSWORD);

        String schema = options.get(AUTHENTICATION_SCHEMA, AUTHENTICATION_SCHEMA_BASIC);
        if (schema != null && schema.equals(AUTHENTICATION_SCHEMA_KERBEROS)) {

            return new KerberosTokenGenerator(username, password, getSpn(address));
        } else {
            // Default try Basic
            return new BasicTokenGenerator(username, password);
        }
    }

    private String getSpn(String address) {
        String spnTemplate = PowerShellPluginConfiguration.getInstance().getKerberosSPNTemplate();
        return spnTemplate.replace("{host}", address);
    }

    private URL getTargetURL(ConnectionOptions options) {
        String scheme = cifsConnectionType == WINRM_HTTP ? "http" : "https";
        String context = options.get(CONTEXT, DEFAULT_WINRM_CONTEXT);
        try {
            return new URL(scheme, address, port, context);
        } catch (MalformedURLException e) {
            throw new WinRMRuntimeIOException("Cannot build a new URL for " + this, e);
        }
    }

    public static HttpConnector newHttpConnector(CifsConnectionType ccType, URL targetURL, TokenGenerator tokenGenerator) {
        switch (ccType) {
            case WINRM_HTTP:
                return new JdkHttpConnector(targetURL, tokenGenerator);
            case WINRM_HTTPS:
                return new LaxJdkHttpConnector(targetURL, tokenGenerator);
        }
        throw new IllegalArgumentException("Invalid CIFS connection type " + ccType);
    }

    @Override
    public int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
        throw new NotImplementedException("Execute method not supported.");
    }

    public void open(final CmdLine commandLine) {
        final String commandLineForExecution = commandLine.toCommandLine(getHostOperatingSystem(), false);
        winRmClient.open(commandLineForExecution);
    }

    @Override
    public void doClose() {
        winRmClient.close();
    }

    public void sendCommandToStream(final String commandLine, boolean shouldEncode,
            final WinRmCapturingOverthereProcessOutputHandler capturingHandler) {
        winRmClient.sendCmdToInputStream(commandLine, shouldEncode);
        winRmClient.getCommandOutput(capturingHandler);
    }

}
