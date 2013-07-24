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
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

public class Pipeline {
    List<Command> commands = new ArrayList<Command>();

    public void addCommand(Command command) {
        commands.add(command);
    }

    public Command addCommandFromString(String command) {
        Command cmd = new Command(command);
        commands.add(cmd);
        return cmd;
    }

    public String getScript() {
        StringBuilder str = new StringBuilder();
        boolean isFirst = true;
        for (Command cmd : commands) {
            if (!isFirst) {
                str.append("|");
            }
            str.append(cmd.getCommand());
            str.append(prepareParams(cmd));
            isFirst = false;

        }

        return str.toString();
    }

    private Object prepareParams(Command cmd) {
        StringBuilder res = new StringBuilder();

        Map<String, Object> params = cmd.getParams();
        for (Entry<String, Object> entry : params.entrySet()) {
            res.append(" -");
            res.append(entry.getKey());
            
            res.append(" ");
            res.append(getParamValue(entry.getValue()));
        }

        return res.toString();
    }

    private Object getParamValue(Object value) {
        String res = null;
        if (value != null && value.getClass().isArray()) {
            res = StringUtils.arrayToCommaDelimitedString((Object[]) value);
        } else if (value instanceof RemotePSObject) {
            RemotePSObject obj = (RemotePSObject) value;
            if (obj.getRefId() != null) {
                res = String.format("(getVarByRef( '%s'))", obj.getRefId());
            } else {
                res = String.format("(deserialize( '%s'))", obj.getXml());
            }
        } else {
            res = value.toString();
        }

        return res;
    }

}
