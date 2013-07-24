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

public class CmdletInfo {

    private String name;
    private String commandType;
    private String psSnapin;
	private String definition;
    private List<CmdletInfoParameterSet> parameterSets;

    public CmdletInfo(String name) {
		this.name = name;
		this.parameterSets = new ArrayList<CmdletInfoParameterSet>();
	}

	private void initParamsets(String cmdletDef, String... paramSetNames) {
        // Separate Cmdlet paramset definitions. Each new paramset must start on new line 
        String[] paramSetsDef = cmdletDef.split(getName());
        int i = 0;
        for (int defIdx = 1; defIdx < paramSetsDef.length; defIdx++) {
            String def = paramSetsDef[defIdx];
            if (!def.isEmpty()) {
                CmdletInfoParameterSet parameterSet = new CmdletInfoParameterSet((paramSetNames != null && paramSetNames.length > i) ? paramSetNames[i] : null, def);
                if (parameterSet != null) {
                    parameterSets.add(parameterSet);
                }
                i++;
            }
        }
    }

    public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public String toString() {
		return name + "->" + getDefinition();
	}

	public List<CmdletInfoParameterSet> getParameters() {
		return parameterSets;
	}

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setPsSnapin(String psSnapin) {
        this.psSnapin = psSnapin;
    }

    public String getPsSnapin() {
        return psSnapin;
    }

    public List<CmdletInfoParameterSet> getParamSets() {
        return parameterSets;
    }

    public void setDefinition(String definition, String... paramSetNames) {
        if (definition == null || "".equals(definition)) {
            throw new IllegalArgumentException(
                    "Cmdlet definishon can't be null or empty string");
        }
        //We are using the cmdlet definition for now, once we have better seriazliation we can use directly the PSObject parameters
        initParamsets(definition, paramSetNames);
        this.definition = definition;
    }
    
    public int getParameterSetIndexByName(String paramSetName){
        if(paramSetName != null){
            int i = 0;
            for (CmdletInfoParameterSet cmdletInfoParameterSet : parameterSets) {
                if(paramSetName.equals(cmdletInfoParameterSet.getName())){
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

}
