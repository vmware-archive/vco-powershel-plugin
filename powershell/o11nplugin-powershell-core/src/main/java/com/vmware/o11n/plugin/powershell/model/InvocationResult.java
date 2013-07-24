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

import java.util.ArrayList;
import java.util.List;

import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

@VsoObject(description = "Represents the result from an execution fo PowerShell script")
public class InvocationResult {

    // status of execution
    private InvocationState invocationState;

    private List<String> errors;

    private RemotePSObject results;

    private String hostOutput;

    public InvocationResult(InvocationState status, List<String> errors, RemotePSObject result, String hostOutput) {
        this.invocationState = status;

        this.errors = new ArrayList<String>();
        if (errors != null) {
            this.errors.addAll(errors);
        }

        this.results = result;
        this.hostOutput = hostOutput;
    }

    @VsoMethod (description="Status of execution of the script. Possible values are (Completed, Failed).")
    public InvocationState getInvocationState() {
        return invocationState;
    }

    @VsoMethod (description="Returns list of objects returned by PowerShell engine after successfull invocation.")
    public RemotePSObject getResults() {
        return results;
    }

    @VsoMethod (description="Returns list of errors reported by powershell engine during script invocation.")
    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void setHostOutput(String hostOutput) {
        this.hostOutput = hostOutput;
    }

    @VsoMethod(description = "Returns output of script execution as it appears on the powershell console.")
    public String getHostOutput() {
        return hostOutput;
    }

}
