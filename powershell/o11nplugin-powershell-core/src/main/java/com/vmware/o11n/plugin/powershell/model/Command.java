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

import java.util.Hashtable;
import java.util.Map;

import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

@VsoObject(description="Represents a PowerShell command that can be run on the remote PowerShell host.")
public class Command {

	private String command;
	private Map<String, Object> params = new Hashtable<String, Object>();

	@VsoConstructor(description="Creates a PowerShellCommand instance that encapsulates the command.")
	public Command(@VsoParam(description="The command as a String") String command) {
		this.command = command;
	}

	@VsoMethod(description="Returns the String that this Command object was initialy created.")
	public String getCommand() {
		return command;
	}

	@VsoMethod(description="Adds a new parameter to the list of params for this PowerShellCommand instance.")
	public Command addParameter(String paramName, Object value) {
		params.put(paramName, value);
		return this;
	}

    @VsoMethod(description="Returns all Parameters as a Map.")
	public Map<String, Object> getParams() {
		return params;
	}
}
