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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdletInfoParameter {

	private String name;
	private String type;
	private boolean isMandatory;
	private String definition;

	private static final Pattern namePattern = Pattern.compile("-{1}(\\w+)");;
	private static final Pattern typePattern = Pattern
			.compile("([\\w\\[\\]]+)(?=>{1})");

	public CmdletInfoParameter(String definition) {
		if (definition == null || "".equals(definition)) {
			throw new IllegalArgumentException(
					"Defininition should not be null or empty string");
		}
		this.definition = definition;
		init();
	}

	private void init() {
		// String[] definitionStrings = definition.split(" (?=(\\[))");
		this.isMandatory = !definition.startsWith("[");
		this.name = extract(namePattern, definition);
		if (this.name != null) {
			this.name = this.name.substring(1);
		}
		this.type = extract(typePattern, definition);
		// If the type is missing this means SwitchParameter
		if (this.type == null) {
			this.type = "SwitchParameter";
		}
		System.out.println(definition + "->" + isMandatory);
	}

	private String extract(Pattern p, String definition) {
		Matcher nameMatcher = p.matcher(definition);
		String result = null;
		if (nameMatcher.find()) {
			result = nameMatcher.group();
		}
		return result;
	}

	public String getDefinition() {
		return definition;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

    @Override
    public int hashCode() {
        int hash = 79;
        if(definition != null){
            hash += definition.hashCode();
        }
        if(name != null){
            hash += name.hashCode(); 
        }
        if(type != null){
            hash += type.hashCode();
        }
        if(isMandatory){
            hash += hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof CmdletInfoParameter){
            CmdletInfoParameter o = (CmdletInfoParameter) obj;
            boolean equalDef = (this.definition != null && o.definition != null && this.definition.equals(o.definition)) 
                                    || (this.definition == null && o.definition == null);
            if(equalDef){
                boolean equalName = (this.name != null && o.name != null && this.name.equals(o.name))
                                        || (this.name == null && o.name == null);
                if(equalName){
                    boolean equalType = (this.type != null && o.type!= null && this.type.equals(o.type))
                    || (this.type == null && o.type == null);
                    
                    return equalType && (this.isMandatory == o.isMandatory);//true if all properties has one and the same data
                }
            }
        }
        return false;
    }
}
