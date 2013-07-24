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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.model.client.IVSOFactoryClient;
import ch.dunes.model.dunes.ScriptModule;
import ch.dunes.model.dunes.ScriptModuleCategory;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.sdk.scriptmodule.ScriptModuleBuilder;

public class ScriptActionGenerator {
    static final String PARAM_HOST = "host";
    static final String PARAM_SESSIONID = "sessionId";
    static final String PARAM_SCRIPT = "psScript";

    private static final String ACT_EXECUTE_PSSCRIPT_LOG_RESULT = "" +
    String.format( "return System.getModule(\"com.vmware.library.powershell\").invokeScript( %s,%s,%s) ;",
            PARAM_HOST, PARAM_SCRIPT, PARAM_SESSIONID);

    static final Logger log = LoggerFactory.getLogger(CmdletActionGenerator.class);

    public ScriptModule generateAction(IVSOFactoryClient factory, String actionName, String categoryName, String script) {
        Validate.notNull(factory, "Factory can not be null.");
        Validate.notEmpty(actionName, "Action name can not be null or empty.");
        Validate.notEmpty(categoryName, "Category name can not be null or empty.");
        
        ScriptModule module = null;

        try {

            ScriptModuleCategory category = factory.getScriptModuleCategoryForName(categoryName);
            Validate.notNull(category, "Unable to find category with name " + categoryName);
            /*Check if there is already action with the same name*/
            Validate.isTrue( category.getScriptModuleWithName(actionName) == null, "Action with provided name already exists." );

            ScriptModuleBuilder builder = new ScriptModuleBuilder().setName(actionName)
                    .setDescription("Auto generated.")
                    .setResultType(Constants.TYPE_POWER_SHELL_REMOTE_PS_OBJECT);
            
            // Add input params
            // Add system input params
            builder.addParameter(PARAM_HOST, "PowerShell:PowerShellHost");
            builder.addParameter(PARAM_SESSIONID, "string");
            
            script = extractParameters(script, builder);

            builder.setScript(generateActionContent(script));
            
            module = builder.insert(categoryName, factory);
        } catch (RuntimeException exc) {
            log.warn(exc.getMessage());
            throw (RuntimeException) exc;
        } catch (Exception exc) {
            String message = exc.getMessage();
            if (message == null || message.length() == 0) {
                message = "Failed to create action for provided script.";
            }
            log.warn(message);
            throw new RuntimeException(message, exc);
        }

        return module;
    }

    private String generateActionContent(String script) {
        StringBuilder scriptBody = new StringBuilder();
        
        scriptBody.append(String.format("var psScript = ''%n"));
        scriptBody.append(preparePsScript(script)); 
        scriptBody.append(ACT_EXECUTE_PSSCRIPT_LOG_RESULT);

        return scriptBody.toString();
    }

    // Note : {#param_name#} were already replaced 
    private String preparePsScript(String script) {
        StringBuilder scriptBody = new StringBuilder();
        BufferedReader reader = new BufferedReader(new StringReader(script));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                scriptBody.append(String.format("psScript +='%s\\n';%n", line));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scriptBody.toString();
    }

    private String extractParameters(String script, ScriptModuleBuilder builder) {

        // first escape the PS script
        script = script.replace("'", "\\'");
        
        //Then look up for {#paramname#} and extract them
        Pattern pattern = Pattern.compile("\\{#([\\w]*)#\\}");
        Matcher matcher = pattern.matcher(script);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String inputName = matcher.group(1);
            // create input param
            builder.addParameter(inputName, "String");
            matcher.appendReplacement(sb, Matcher.quoteReplacement("' + " + inputName + " + '"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


}
