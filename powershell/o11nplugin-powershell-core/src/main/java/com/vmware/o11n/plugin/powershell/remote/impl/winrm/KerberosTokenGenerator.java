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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.remote.AuthenticationException;
import com.xebialabs.overthere.cifs.winrm.TokenGenerator;

public class KerberosTokenGenerator implements TokenGenerator {
    static final Logger log = LoggerFactory.getLogger(KerberosTokenGenerator.class);

    private static final String SPNEGO_OID = "1.3.6.1.5.5.2";

    private static final String CHARSET_UTF_8 = "UTF-8";

    private NTUser user;

    private String password;

    private Subject subject = null;

    private byte[] serviceTicket = null;

    private String spn;

    public KerberosTokenGenerator(String username, String password, String spn) {
        super();
        this.user = new NTUser(username);
        this.password = password;
        this.spn = spn;
    }


    @Override
    public String generateToken() {
        try {
            try {
                login(user, password);
            } catch (LoginException e) {
                String msg = e.getMessage();
                if (!StringUtils.isNotBlank(msg)) {
                    msg = "Login failed.";
                }
                throw new AuthenticationException(msg, e);
            }

            try {
                initiateSecurityContext();
            } catch (GSSException e) {
                String msg = e.getMessage();
                if (msg == null) {
                    msg = "Login failed";
                }
                throw new AuthenticationException(msg, e);
            }

            //TODO : Hidden option to change it With Negotiate
            return "Kerberos " + new String(Base64.encodeBase64(serviceTicket), CHARSET_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // Authenticate against the KDC using JAAS.
    private void login(final NTUser userName, final String password) throws LoginException {
        this.subject = new Subject();
        LoginContext login;
        login = new LoginContext("", subject, new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        //We may need some more complete mapping between AD user domain and Kerberos realms  
                        String kerbUserSPN = userName.getUserName();
                        if (StringUtils.isNotBlank(userName.getDomain())){
                            kerbUserSPN += "@" + userName.getDomain().toUpperCase(); 
                        }
                        
                        log.debug("Kerberos login name: " + kerbUserSPN);
                        ((NameCallback) callback).setName(kerbUserSPN);
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(password.toCharArray());
                    }
                }
            }
        }, new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                Map<String, String> config = new HashMap<String, String>();
                config.put("useTicketCache", "false");

                return new AppConfigurationEntry[] { new AppConfigurationEntry(
                        "com.sun.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, config) };
            }
        });
        login.login();

    }

    // Begin the initiation of a security context with the target service.
    private void initiateSecurityContext() throws GSSException {
        GSSManager manager = GSSManager.getInstance();
        GSSName gssSPN = manager.createName(spn, null);

        final GSSContext context = manager.createContext(gssSPN, new Oid(SPNEGO_OID), null,
                GSSContext.DEFAULT_LIFETIME);

        // The GSS context initiation has to be performed as a privilegedv action.
        this.serviceTicket = Subject.doAs(subject, new PrivilegedAction<byte[]>() {
            public byte[] run() {
                try {
                    byte[] token = new byte[0];
                    context.requestMutualAuth(true);
                    context.requestCredDeleg(true);
                    return context.initSecContext(token, 0, token.length);
                } catch (GSSException e) {
                    String msg = e.getMessage();
                    if (StringUtils.isBlank(msg)) {
                        msg = "Authentication failed.";
                    }
                    log.error(msg, e);
                    throw new AuthenticationException(msg, e);
                }
            }
        });
    }

    class NTUser {
        String fqUserName;

        public NTUser(String fqUserName) {
            this.fqUserName = fqUserName;
        }

        public String getDomain() {
            String[] user_domain = split();
            return user_domain[1];
        }

        public String getUserName() {
            String[] user_domain = split();
            return user_domain[0];
        }

        private String[] split() {
            String[] user_domain = { "", "" };

            //try "user@domain"
            String[] parts = fqUserName.split("@", 2);
            if (parts.length == 2) {
                user_domain[0] = parts[0];
                user_domain[1] = parts[1];
            } else {
                // Try domain\\user format
                parts = fqUserName.split(Pattern.quote("\\"), 2);
                if (parts.length == 2) {
                    user_domain[0] = parts[1];
                    user_domain[1] = parts[0];
                }
            }

            return user_domain;
        }
    }
}
