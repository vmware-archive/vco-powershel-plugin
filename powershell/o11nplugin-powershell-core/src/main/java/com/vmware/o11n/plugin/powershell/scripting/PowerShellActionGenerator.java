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
package com.vmware.o11n.plugin.powershell.scripting;

import java.rmi.RemoteException;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import ch.dunes.model.dunes.ScriptModule;
import ch.dunes.model.workflow.WorkflowCategory;
import ch.dunes.util.DunesServerException;
import ch.dunes.vso.sdk.api.IPluginFactory;

import com.vmware.o11n.plugin.powershell.model.CmdletInfoHelper;
import com.vmware.o11n.plugin.powershell.model.generate.CmdletActionGenerator;
import com.vmware.o11n.plugin.powershell.model.generate.SampleWorkflowBuilder;
import com.vmware.o11n.plugin.powershell.model.generate.ScriptActionGenerator;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.PluginFactoryAware;
import com.vmware.o11n.plugin.sdk.workflow.WorkflowBuilderExt;

@VsoObject(create = false, singleton = true, description="Generates actions for PowerShell Cmdlets and Scripts")
@Component
public class PowerShellActionGenerator implements PluginFactoryAware {
    
    private AbstractSpringPluginFactory factory;


    public PowerShellActionGenerator() {
        // TODO Auto-generated constructor stub
    }

    public static PowerShellActionGenerator createScriptingSingleton(IPluginFactory factory) {
        return ((com.vmware.o11n.plugin.PowerShellPluginFactory) factory)
                .createScriptingObject(PowerShellActionGenerator.class);
    }


    @VsoMethod(description="Generates vCO action for provided Cmdlet definition.",showInApi=false)
    public void createActionForCmdlet(PowerShellCmdlet cmdlet, String parameterSetName, String actionName,
            String moduleName, boolean generateWorkflow, WorkflowCategory targetFolder ) throws RemoteException, LoginException, DunesServerException {
    
        if (cmdlet == null ) { 
            throw new IllegalArgumentException( "Invalid cmdlet definition.");
        }

        assertNotBlank( actionName, "Action name ");
        assertNotBlank( moduleName, "Module name ");
        
        String sampleWorkflowName =  "Execute CmdLet "  + actionName;     
        if (generateWorkflow ) { 
            if (targetFolder == null ) { 
                throw new IllegalArgumentException( "TargetFolder can't be null or emtpy.");
            } else {
                if ( WorkflowBuilderExt.workflowExists(targetFolder, sampleWorkflowName) ){
                    throw new RuntimeException (String.format("Workflow %s already exists.", sampleWorkflowName)); 
                }
            }
            
        }

        CmdletActionGenerator ag = new CmdletActionGenerator(cmdlet.getInfo(), parameterSetName);
        ScriptModule module = ag.generateAction(factory.getVsoFactoryClient(), actionName, moduleName);
        
        if (generateWorkflow == true) {
            SampleWorkflowBuilder wb = new SampleWorkflowBuilder();
            if (!CmdletInfoHelper.isSystemSnapIn(cmdlet.getPsSnapin())){
                wb.setThirdPartySnapinName(cmdlet.getPsSnapin());
            }
            wb.buildWorkflow(module, sampleWorkflowName, factory.getVsoFactoryClient(), targetFolder);
        }
    }


    @VsoMethod(description="Generates vCO action for provided PowerShell script.")
    public void createActionForScript(String actionName, String script, String moduleName, boolean generateWorkflow,
            WorkflowCategory targetFolder) throws RemoteException, LoginException, DunesServerException {

        assertNotBlank( actionName, "Action name ");
        assertNotBlank( moduleName, "Module name ");

        String sampleWorkflowName =  "Invoke Script " + actionName;     
        if (generateWorkflow ) { 
            if (targetFolder == null ) { 
                throw new IllegalArgumentException( "TargetFolder can't be null or emtpy.");
            } else {
                if ( WorkflowBuilderExt.workflowExists(targetFolder, sampleWorkflowName) ){
                    throw new RuntimeException (String.format("Workflow %s already exists.", sampleWorkflowName)); 
                }
            }
        }

        ScriptActionGenerator ag = new ScriptActionGenerator();
        ScriptModule module = ag.generateAction(factory.getVsoFactoryClient(), actionName, moduleName, script);
        if (generateWorkflow == true) {
            SampleWorkflowBuilder wb = new SampleWorkflowBuilder();
            wb.buildWorkflow(module, sampleWorkflowName, factory.getVsoFactoryClient(), targetFolder); 
        }
        
    }


    @Override
    public AbstractSpringPluginFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setPluginFactory(AbstractSpringPluginFactory arg0) {
        this.factory = arg0;
    }
    

    private void assertNotBlank( String param, String  paramName){
        if ( StringUtils.isBlank( param ) ){
            throw new IllegalArgumentException( paramName + " can't be null or emtpy.");
        }    
    }
    
}
