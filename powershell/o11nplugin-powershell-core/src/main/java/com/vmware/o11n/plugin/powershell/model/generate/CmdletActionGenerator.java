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

package com.vmware.o11n.plugin.powershell.model.generate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.model.client.IVSOFactoryClient;
import ch.dunes.model.dunes.ScriptModule;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.powershell.model.CmdletInfo;
import com.vmware.o11n.plugin.powershell.model.CmdletInfoParameter;
import com.vmware.o11n.plugin.sdk.scriptmodule.ScriptModuleBuilder;

public class CmdletActionGenerator {
    private static final Logger log = LoggerFactory.getLogger(CmdletActionGenerator.class);

    private static final String ARRAY_MODIFIER = "[]";
    private static final String ACT_HEADER = " var oSession = %s.getSession(%s);\n"
            + " var cmd = oSession.addCommandFromString('%s');\n";
    private static final String ACT_PARAM_TEMPLATE = " if (%s)\n" + "   cmd.addParameter('%s', %s)\n";
    private static final String ACT_SWITCHPARAM_TEMPLATE = " if (%s != null )\r\n" + 
                                                    		" {\r\n" + 
                                                    		"    val = ':$false'\r\n" + 
                                                    		"    if (%s == true) {\r\n" + 
                                                    		"        val = ':$true'\r\n" + 
                                                    		"    }\r\n" + 
                                                    		"    cmd.addParameter('%s'+val,'')\r\n" + 
                                                    		" }\r\n" + 
                                                    		"";
    private static final String ACT_INVOKE = "" +
                                        	"var res; \n" +
                                        	"if ( host.inPipeline(sessionId) == false )" +
                                        	"{ \n" +
                                        	"  res = System.getModule(\"com.vmware.library.powershell\").invokeCommand(host,sessionId) ;\n" +
                                        	"}\n" +
                                        	"return res;\n";

    // Cmdlet params could be VCO reserved word like "Debug"
    private static final String ACT_PARAM_NAME_PREFIX = "ps";

    private static final String ACT_PARAM_NAME_HOST = "host";

    private static final String ACT_PARAM_NAME_SESSION_ID = "sessionId";

    private CmdletInfo info;

    private int paramSetIdx;

    public CmdletActionGenerator(CmdletInfo info, String paramSetName) {
        this.info = info;
        this.paramSetIdx = info.getParameterSetIndexByName(paramSetName);
        if (this.paramSetIdx < 0) {
            this.paramSetIdx = 0;//force for the first parameter set if parameterSetName not found
            log.warn(String.format("Parameter set %s can not be found. Default will be used.", paramSetName));
        }
    }

    public ScriptModule generateAction(IVSOFactoryClient factory, String actionName, String categoryName) {
        Validate.notNull(factory, "Factory can not be null.");
        Validate.notEmpty(actionName, "Action name must be provided.");
        Validate.notEmpty(categoryName, "Category name must be provided.");

        ScriptModule module = null;
        try {
            ScriptModuleBuilder builder = new ScriptModuleBuilder().setName(actionName)
                                                                       .setDescription("Auto generated.")
                                                                       .setResultType(Constants.TYPE_POWER_SHELL_REMOTE_PS_OBJECT);

            // Add system input params
            builder.addParameter(ACT_PARAM_NAME_HOST, "PowerShell:PowerShellHost");
            builder.addParameter(ACT_PARAM_NAME_SESSION_ID, "string");
            
            // Add cmdlet input params
            buildCmdletParams(builder);

            builder.setScript(buildCmdletScript());

            module = builder.insert(categoryName, factory);
        } catch (RuntimeException exc) {
            log.warn(exc.getMessage());
            throw (RuntimeException) exc;
        } 

        return module;
    }

    private  void  buildCmdletParams(ScriptModuleBuilder builder) {

        for (CmdletInfoParameter cmdletParam : getParameters()) {

            String psType = cmdletParam.getType();

            String vcoType = toVcoType(psType);
            if (vcoType != null) {
                // TODO add required
                builder.addParameter(getVcoParamName(cmdletParam.getName()), vcoType);
            } else {
                log.warn("Unsupported parameter type :" + psType);
            }
        }
    }

    private List<CmdletInfoParameter> getParameters() {
        return info.getParamSets().get(paramSetIdx).getParameters();
    }

    private String buildCmdletScript() {
        StringBuilder res = new StringBuilder(String.format(ACT_HEADER, ACT_PARAM_NAME_HOST, ACT_PARAM_NAME_SESSION_ID,
                info.getName()));

        List<CmdletInfoParameter> parameters = getParameters();
        for (CmdletInfoParameter param : parameters) {
            // Skip unsupported parameter types
            String psType = param.getType();
            if (toVcoType(psType) == null) {
                log.warn("Unsupporter type : " + psType);
                continue;
            }

            String vcoParamName = getVcoParamName(param.getName());
            String psParamName = param.getName();
            if ("SwitchParameter".equals(psType)) {
                res.append(String.format(ACT_SWITCHPARAM_TEMPLATE, vcoParamName, vcoParamName, psParamName));
            } else if ("String".equals(psType)) {
                res.append(String.format(ACT_PARAM_TEMPLATE, vcoParamName, psParamName, "\"'\" + " + vcoParamName
                        + "+ \"'\""));
            } else {
                res.append(String.format(ACT_PARAM_TEMPLATE, vcoParamName, psParamName, vcoParamName));
            }
        }
        res.append(ACT_INVOKE);

        return res.toString();
    }

    private static boolean isArrayType(String psType) {
        return psType.endsWith(ARRAY_MODIFIER);
    }

    // Add prefix to PS parameter name, because some parameter names like
    // "Debug" are reserved for VCO
    private String getVcoParamName(String name) {
        return ACT_PARAM_NAME_PREFIX + name;
    }

    private static final Map<String, String> psToVcoType = new HashMap<String, String>();
    // TODO: complete list of types
    static {
        psToVcoType.put("Int16", "number");
        psToVcoType.put("System.Int16", "number");

        psToVcoType.put("Int32", "number");
        psToVcoType.put("System.Int32", "number");

        psToVcoType.put("UInt32", "number");
        psToVcoType.put("System.UInt32", "number");

        psToVcoType.put("Int64", "number");
        psToVcoType.put("System.Int64", "number");

        psToVcoType.put("Double", "number");
        psToVcoType.put("System.Double", "number");

        psToVcoType.put("Byte", "number");
        psToVcoType.put("System.Byte", "number");

        psToVcoType.put("Char", "string");
        psToVcoType.put("System.Char", "string");

        psToVcoType.put("String", "string");
        psToVcoType.put("System.String", "string");

        psToVcoType.put("Boolean", "boolean");
        psToVcoType.put("System.Boolean", "boolean");

        psToVcoType.put("SwitchParameter", "boolean");
        psToVcoType.put("System.Management.Automation.SwitchParameter", "boolean");
    }

    private String toVcoType(String psType) {
        String res = null;
        if (isArrayType(psType)) {
            String type = toVcoType(getComponentType(psType));
            // if this is not supported type also array/type is  not supported
            if (type != null) {
                // Invoking powershell script always return REMOTE_PS_OBJECT (not Array/REMOTE_PS_OBJECT)
                // that's why we genearate input parameters as single REMOTE_PS_OBJECT_TYPE
                if (Constants.TYPE_POWER_SHELL_REMOTE_PS_OBJECT.equals(type)) {
                    return type;
                } else {
                    res = "Array/" + type;
                }
            }
        } else {
            res = psToVcoType.get(psType);
            //all other types will be considered complex PowerShell types
            if (res == null) {
                res = Constants.TYPE_POWER_SHELL_REMOTE_PS_OBJECT;
            }
        }

        return res;
    }

    private String getComponentType(String psType) {
        if (isArrayType(psType)) {
            return psType.substring(0, psType.lastIndexOf(ARRAY_MODIFIER));
        }
        return psType;
    }

}
