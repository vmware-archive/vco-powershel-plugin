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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.dunes.model.client.IVSOFactoryClient;
import ch.dunes.model.dunes.ScriptModule;
import ch.dunes.model.presentation.PresentationStep;
import ch.dunes.model.workflow.WorkflowCategory;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.sdk.workflow.ScriptingBoxItem;
import com.vmware.o11n.plugin.sdk.workflow.WorkflowBuilderExt;

public class SampleWorkflowBuilder {

    static final Logger log = LoggerFactory.getLogger(SampleWorkflowBuilder.class);

    private static final String WI_ADD_PS_SNAP_IN = "addPSSnapIn";

    private static final String WI_OPEN_SESSION = "openSession";

    private static final String WI_CLOSE_SESSION = "closeSession";

    private static final String WI_CHECK_ERRORS_ACTION = "checkErrorsAction";

    private static final String WI_END_ITEM = "end";

    private static final String WI_EXCEPTION_END = "exceptionEnd";

    private static final String ATTR_SESSION_ID = "sessionId";

    private static final String ATTR_ERROR_CODE = "errorCode";

    private static final String ATTR_SNAPIN_NAME = "attrSnapinName";

    private static final String PARAM_HOST = "host";

    private static final String PARAM_OUTPUT = "output";

    private static final String ACTION_CHECK_FOR_ERRORS_SCRIPT = "host.closeSession(sessionId);\r\n"
            + "if (errorCode) {\r\n" + "    System.error(errorCode);\r\n" + "    throw errorCode;\r\n" + "}";

    private WorkflowBuilderExt wb = new WorkflowBuilderExt();

    private String snapinName;

    public void setThirdPartySnapinName(String snapinName) {
        this.snapinName = snapinName;
    }

    public void buildWorkflow(ScriptModule module, String workflowName, IVSOFactoryClient factory,
            WorkflowCategory targetFolder) {
        try {
            wb.setName(workflowName);

            // Create wf output param
            wb.addOutParameter("output", Constants.TYPE_POWER_SHELL_REMOTE_PS_OBJECT);

            // create Workflow Attributes
            //sessionId attribure
            wb.addAttribute(ATTR_SESSION_ID, "string");
            //errorCode attribute
            wb.addAttribute(ATTR_ERROR_CODE, "string");

            //snapinName attr 
            if (null != this.snapinName) {
                wb.addAttribute(ATTR_SNAPIN_NAME, "string", snapinName);
            }

            // create nodes
            // create openSession
            wb.createInvokeScriptModuleItem(WI_OPEN_SESSION, GenerateConstants.POWERSHELL_MODULE_NAME,
                    GenerateConstants.ACT_OPEN_SESSION, factory).setLocation(140.0, 100.0);
            wb.bindItemInParameter(WI_OPEN_SESSION, "host", PARAM_HOST);
            wb.bindItemOutParameter(WI_OPEN_SESSION, "actionResult", ATTR_SESSION_ID);

            // create scripting for - addPsSnapin
            double offset = 0.0;
            if (null != this.snapinName) {
                offset = 50.0;
                wb.createInvokeScriptModuleItem(WI_ADD_PS_SNAP_IN, GenerateConstants.POWERSHELL_MODULE_NAME,
                        GenerateConstants.ACT_ADD_PS_SNAPIN, factory).setLocation(140.0, 150.0);
                wb.bindItemInParameter(WI_OPEN_SESSION, "host", PARAM_HOST);
                wb.bindItemInParameter(WI_ADD_PS_SNAP_IN, "sessionId", ATTR_SESSION_ID);
                wb.bindItemInParameter(WI_ADD_PS_SNAP_IN, "psName", ATTR_SNAPIN_NAME);

                wb.bindItemOutParameter(WI_ADD_PS_SNAP_IN, "actionResult", PARAM_OUTPUT);
                wb.bindItemExceptionAttribute(WI_ADD_PS_SNAP_IN, ATTR_ERROR_CODE);
            }

            // create scripting for - Invoke action 
            String wiInokedActionName = module.getName();
            wb.createInvokeScriptModuleItem(wiInokedActionName, module).setLocation(140.0, 150.0 + offset);
            wb.promoteItemParameters(wiInokedActionName, Arrays.asList("sessionId"));
            wb.bindItemInParameter(wiInokedActionName, "sessionId", ATTR_SESSION_ID);
            wb.bindItemOutParameter(wiInokedActionName, "actionResult", PARAM_OUTPUT);
            wb.bindItemExceptionAttribute(wiInokedActionName, ATTR_ERROR_CODE);

            // create closeSession
            wb.createInvokeScriptModuleItem(WI_CLOSE_SESSION, GenerateConstants.POWERSHELL_MODULE_NAME,
                    GenerateConstants.ACT_CLOSE_SESSION, factory).setLocation(140.0, 200.0 + offset);
            wb.bindItemInParameter(WI_CLOSE_SESSION, "sessionId", ATTR_SESSION_ID);
            wb.bindItemInParameter(WI_CLOSE_SESSION, "host", PARAM_HOST);

            // create scripting box for ErrorCheck 
            ScriptingBoxItem item = wb.createScriptingBoxItem(WI_CHECK_ERRORS_ACTION, ACTION_CHECK_FOR_ERRORS_SCRIPT)
                    .setLocation(140.0, 250.0 + offset);
            item.addInParameter(ATTR_ERROR_CODE, ATTR_ERROR_CODE, "string");
            item.addInParameter(ATTR_SESSION_ID, ATTR_SESSION_ID, "string");
            item.addInParameter(PARAM_HOST, PARAM_HOST, Constants.TYPE_POWERSHELL_HOST);
            wb.bindItemExceptionAttribute(WI_CHECK_ERRORS_ACTION, ATTR_ERROR_CODE);

            wb.createEndItem(WI_END_ITEM, 180.0, 300.0 + offset);

            wb.createErrorEndItem(WI_EXCEPTION_END, 350.0, 240.0 + offset, ATTR_ERROR_CODE);

            if (null != snapinName) {
                wb.connectItem(WI_OPEN_SESSION, WI_ADD_PS_SNAP_IN);
                wb.connectItem(WI_ADD_PS_SNAP_IN, wiInokedActionName);
                wb.connectErrorHandlerItem(WI_ADD_PS_SNAP_IN, WI_CLOSE_SESSION);
            } else {
                wb.connectItem(WI_OPEN_SESSION, wiInokedActionName);
            }
            wb.connectItem(wiInokedActionName, WI_CLOSE_SESSION);
            wb.connectErrorHandlerItem(wiInokedActionName, WI_CLOSE_SESSION);
            wb.connectItem(WI_CLOSE_SESSION, WI_CHECK_ERRORS_ACTION);
            wb.connectItem(WI_CHECK_ERRORS_ACTION, WI_END_ITEM);
            wb.connectErrorHandlerItem(WI_CHECK_ERRORS_ACTION, WI_EXCEPTION_END);

            wb.setRootItemName(WI_OPEN_SESSION);

            PresentationStep step1 = wb.addStep("Execute PowerShell script", "");
            wb.addGroup(step1, "Host", "", Arrays.asList(PARAM_HOST));
            List<String> params = wb.getInParameterNames();
            params.remove(PARAM_HOST);
            wb.addGroup(step1, "Script Parameters", "", params);

            wb.insertWorkflow(factory, targetFolder);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                String message = e.getMessage();
                if (message == null || message.length() == 0) {
                    message = "Failed to build local workflow proxy.";
                }
                throw new RuntimeException(message, e);
            }
        }
    }

}