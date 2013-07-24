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

package com.vmware.o11n.plugin.powershell.config;

import java.io.Serializable;

import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;


@VsoObject(description="Encapsulates the configuration for a remote PowerShell host")
public class PowerShellHostConfig implements Serializable, Comparable<PowerShellHostConfig> {

    private static final long serialVersionUID = 1L;

    public static final String POWERSHELL_HOST_SSH = "SSH";
    public static final String POWERSHELL_HOST_WINRM = "WinRM";
    
	private String id ;
	private String name;
	private String type;
	private String transportProtocol;
	private String port;
	
	// Shared session vs user per session
    private AuthorizationMode authorizationMode;
	
    private String connectionURL;
	
	private String username;
	private String password;
	
	//Specifies authentication mechanism used when communicating with remote machine.
	//Possible options are Basic, Kerberos.
	private String authentication;

    public PowerShellHostConfig(String id) {
        this.id = id;
    }

    public PowerShellHostConfig() {
        this.id = null;
        this.setAuthentication("Basic");
    }	

	public PowerShellHostConfig(final PowerShellHostConfig config) {
	    this.id  = config.id;
	    this.name = config.name;
	    this.type = config.type;
	    this.transportProtocol = config.transportProtocol;
	    this.port = config.port;
	    this.authorizationMode = config.authorizationMode;
	    this.connectionURL = config.connectionURL;
	    this.username = config.username;
	    this.password = config.password;        
	    this.authentication = config.authentication;        
    }

    public void setName(String name) {
		this.name = name;
	}

    @VsoProperty(description="Logical name given to the remote PowerShell Machine")
	public String getName() {
		return name;
	}

    @VsoProperty(description="The type of the communication protocol i.e. WinRM or SSH")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@VsoProperty(description="The transport protocol in case of WinRM type of communication. Can be either HTTPS or HTTP")
    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @VsoProperty(description="The port on which to connect in case of WinRM type of communication.")
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setAuthorizationMode(AuthorizationMode autorizationMode) {
        this.authorizationMode = autorizationMode;
    }

    @VsoProperty(description="The authorization mode for this configuration")
    public AuthorizationMode getAuthorizationMode() {
        return authorizationMode;
    }    
    
    @Deprecated
    public void setAutorizationMode(AutorizationMode autorizationMode) {
        this.authorizationMode = AutorizationMode.getAuthorizationMode(autorizationMode);
    }

    @Deprecated
    @VsoProperty(description="The authorization mode for this configuration")
    public AutorizationMode getAutorizationMode() {
        return AutorizationMode.fromString(authorizationMode.getCaption());
    }
    
    @VsoProperty(description="The IP/Hostname of the remote machine")
	public String getConnectionURL() {
		return connectionURL;
	}

	public void setConnectionURL(String connectionURL) {
		this.connectionURL = connectionURL;
	}

	@VsoProperty(description="Specifies authentication mechanism used when communicating with PowerShell host. WinRM protocol supported Basic and Kerberos.")
	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authenticationSchema) {
		this.authentication = authenticationSchema;
	}

	@VsoProperty(description="The username of the user that logs to the remote PowerShell Machine")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@VsoProperty(description="The password to use to log into the remote PowerShell Machine")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "PowerShellHost[name: " + getName() + ", type: " + type
				+ ", connectionURL: " + connectionURL + ", username: "
				+ username + "]";
	}

	@Override
	public int compareTo(PowerShellHostConfig other) {
		return getName().compareTo(other.getName());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((connectionURL == null) ? 0 : connectionURL.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result	+ ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((transportProtocol == null) ? 0 : transportProtocol.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result	+ ((username == null) ? 0 : username.hashCode());
		result = prime * result   + ((authorizationMode == null) ? 0 : authorizationMode.hashCode());
		result = prime * result   + ((getAuthentication() == null) ? 0 : getAuthentication().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		
		PowerShellHostConfig other = (PowerShellHostConfig) obj;
		if (connectionURL == null) {
			if (other.connectionURL != null){
				return false;
			}
		} else if (!connectionURL.equals(other.connectionURL)){
			return false;
		}
		
		if (id == null) {
			if (other.id != null){
				return false;
			}
		} else if (!id.equals(other.id)){
			return false;
		}
		
		if (name == null) {
			if (other.name != null){
				return false;
			}
		} else if (!name.equals(other.name)){
			return false;
		}
		
		if (password == null) {
			if (other.password != null){
				return false;
			}
		} else if (!password.equals(other.password)){
			return false;
		}
		
		if (type == null) {
            if (other.type != null){
                return false;
            }
        } else if (!type.equals(other.type)){
            return false;
        }
            
        if (transportProtocol == null) {
            if (other.transportProtocol != null){
                return false;
            }
        }else if (!transportProtocol.equals(other.transportProtocol)){
            return false;
        }
        
        if (port == null) {
            if (other.port != null){
                return false;
            }
        } else if (!port.equals(other.port)){
			return false;
        }
        
		if (username == null) {
			if (other.username != null){
				return false;
			}
		} else if (!username.equals(other.username)){
			return false;
		}
		
        if (authorizationMode == null) {
            if (other.authorizationMode != null){
                return false;
            }
        } else if (!authorizationMode.equals(other.authorizationMode)){
            return false;
        }
        
        if (authentication == null) {
            if (other.authentication != null){
                return false;
            }
        } else if (!authentication.equals(other.authentication)){
            return false;
        }
		return true;
	}

}
