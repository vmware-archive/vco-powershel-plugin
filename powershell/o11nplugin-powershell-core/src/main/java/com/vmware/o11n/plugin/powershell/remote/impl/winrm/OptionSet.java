/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import com.google.common.collect.Lists;
import com.xebialabs.overthere.cifs.winrm.KeyValuePair;
import com.xebialabs.overthere.cifs.winrm.Namespaces;

/**
 */
public enum OptionSet {

	OPEN_SHELL(Lists.newArrayList(new KeyValuePair("WINRS_NOPROFILE", "FALSE"), new KeyValuePair("WINRS_CODEPAGE", "437"))),
	RUN_COMMAND(Lists.newArrayList(new KeyValuePair("WINRS_CONSOLEMODE_STDIN", "TRUE")));

	private final List<KeyValuePair> keyValuePairs;


	OptionSet(List<KeyValuePair> keyValuePairs) {
		this.keyValuePairs = keyValuePairs;
	}

	public Element getElement() {
		final Element optionSet = DocumentHelper.createElement(QName.get("OptionSet", Namespaces.NS_WSMAN_DMTF));
		for (KeyValuePair p : keyValuePairs) {
			optionSet.addElement(QName.get("Option", Namespaces.NS_WSMAN_DMTF)).addAttribute("Name", p.getKey()).addText(p.getValue());
		}
		return optionSet;
	}
}
