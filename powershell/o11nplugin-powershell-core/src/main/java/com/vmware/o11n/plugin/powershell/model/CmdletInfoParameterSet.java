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

public class CmdletInfoParameterSet {
    
    private String name;
    private String definition;
    private List<CmdletInfoParameter> parameters;

    public CmdletInfoParameterSet(String name, String definition) {
        this.name = name;
        this.parameters = new ArrayList<CmdletInfoParameter>();
        this.definition = definition;
        
    }

    public List<CmdletInfoParameter> getParameters() {
        init();
        return parameters;
    }

    private void init() {
        if(parameters.size() <= 0){
            String[] str = this.definition.trim().split(" (?=([\\[\\-]))");
            parameters = new ArrayList<CmdletInfoParameter>();
            //skip the cmdlet name and process only parameters
            for (int i = 0; i < str.length; i++) {
                parameters.add(new CmdletInfoParameter(str[i]));
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public int hashCode() {
        int hash = 58;
        if(name != null){
            hash += name.hashCode();
        }
        
        init();
        
        for (CmdletInfoParameter parameter : parameters) {
            hash += parameter.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if((obj == null) || (obj.getClass() != this.getClass())){
            return false;
        }
        CmdletInfoParameterSet o = (CmdletInfoParameterSet) obj;
        
        o.init();
        init();
        return ((this.name == null && o.getName() == null) || this.name.equals(o.getName())) && parameters.equals(o.getParameters());
    }

    @Override
    public String toString() {
        return name + "->" + definition;
    }
    
    
}
