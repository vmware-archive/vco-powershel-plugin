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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.powershell.model.result.PSObject;


public class CmdletInfoHelper {

    static final Logger log = LoggerFactory.getLogger(CmdletInfoHelper.class);

    private static final int CMDLET_SERIALIZATION_LEVEL = 3;

    private static final String ADD_PS_SNAPIN_S = "Add-PsSnapin %s";

    private static final String SCRIPT_GET_COMMAND_INFO = " function convertCmdlet($cmdlets) {\r\n" + 
    		"        $res = @();\r\n" + 
    		"        $cmdlets | ForEach-Object {$res += createCmdletTO($_)}\r\n" + 
    		"        \r\n" + 
    		"        return $res;\r\n" + 
    		"    }\r\n" + 
    		"    \r\n" + 
    		"    function createCmdletTO($cmdlet){\r\n" + 
    		"        $object = New-Object Object\r\n" + 
    		"        Add-Member -inputObject $object -memberType NoteProperty -name Name -value $cmdlet.Name\r\n" + 
    		"        Add-Member -inputObject $object -memberType NoteProperty -name CommandType -value $cmdlet.CommandType.ToString()\r\n" + 
    		"        Add-Member -inputObject $object -memberType NoteProperty -name PSSnapIn -value $cmdlet.PSSnapIn.Name\r\n" + 
    		"        Add-Member -inputObject $object -memberType NoteProperty -name Definition -value $cmdlet.Definition\r\n" + 
    		"        \r\n" + 
    		"        $paramSets = @();\r\n" + 
    		"        $cmdlet.ParameterSets | ForEach-Object {$paramSets += $_.Name}\r\n" + 
    		"        Add-Member -inputObject $object -memberType NoteProperty -name ParameterSets -value $paramSets\r\n" + 
    		"        \r\n" + 
    		"        return $object\r\n" + 
    		"    }\r\n" + 
    		"    \r\n" + 
    		"    function Get-CommandInfo( [string] $CmdletName, [string] $SnapinName){\r\n" + 
    		"        [string] $snapinFilter = '';\r\n" + 
    		"        \r\n" + 
    		"        if ($CmdletName){\r\n" + 
    		"            $cmdlets = Get-Command -commandType Cmdlet $CmdletName;\r\n" + 
    		"        } else {\r\n" + 
    		"            $cmdlets = Get-Command -commandType Cmdlet;\r\n" + 
    		"        }\r\n" + 
    		"    \r\n" + 
    		"        $result = $cmdlets\r\n" + 
    		"        if ($SnapinName) {\r\n" + 
    		"            $result =  $cmdlets | Where-Object {$_.PSSNapin -like $SnapinName};\r\n" + 
    		"        }\r\n" + 
    		"        \r\n" + 
    		"        if ($result) {\r\n" + 
    		"            return ConvertCmdlet($result)\r\n" + 
    		"        } \r\n" + 
    		"    }    \r\n" +
    		"\r\n" +
    		"Get-CommandInfo %s %s \r\n" +
    		"\r\n";
    		

    private static final String SCRIPT_GETPSSNAPIN = "function getAllSnapins(){ " + 
    													"    $snapins=@() ;" +
    													"    $snapins+=Get-PSSnapin  ;" +
                                                		"    $snapins+=Get-PSSnapin -registered ;" + 
                                                		"    return $snapins ;" + 
                                                		"} ;" + 
                                                		" ;" + 
                                                		"getAllSnapins %s";
    private static final String SCRIPT_GETPSSNAPIN_FILTER = " | Where-Object {$_.Name -like \"%s\"}";
    
    public static List<SnapInInfo> getSnapin(Session session, String pssnapinName) {
        log.debug("Loading snapin info " +  pssnapinName);
        if (session == null) {
            throw new IllegalArgumentException("Parameter can not be null : " + session);
        }

        String snapinFilter = "";
        if (pssnapinName != null && !(pssnapinName.trim().isEmpty())) {
            
            snapinFilter = String.format(SCRIPT_GETPSSNAPIN_FILTER, pssnapinName);
        }
        String cmd = String.format(SCRIPT_GETPSSNAPIN, snapinFilter);
        InvocationResult res = session.invoke(cmd);
        if (res.hasErrors()) {
            throw new PowerShellException("Unable to resolve Snapin definitions. " + res.getErrors().get(0));
        }

        List<SnapInInfo> result = convertResultToSnapInList(res);
        log.debug("Loading snapin info completed [ " +  pssnapinName + "]");
        return result;
    }

    private static List<SnapInInfo> convertResultToSnapInList(InvocationResult res){
        log.debug("convertResultToSnapInList : Started");
        List<SnapInInfo> snapins = new ArrayList<SnapInInfo>();
        RemotePSObject results = res.getResults();
        
        if (results == null) {
            return snapins;
        }
        
        Object root = results.getRootObject();
        if (root instanceof List<?>) {
            List<?> list = (List<?>) root;
            for (Object obj : list ) {
                if(obj instanceof PSObject){
                    snapins.add(convertResultToSnapIn(obj));
                }
            }
        } else {
            snapins.add(convertResultToSnapIn(root));
        }
        
        log.debug("convertResultToSnapInList : Completed");
        return snapins;
    }
    
    private static SnapInInfo convertResultToSnapIn(Object obj) {

        assert(obj instanceof PSObject );
        PSObject psSnapIn = (PSObject) obj; 
        
        SnapInInfo res = new SnapInInfo();
        res.setName(psSnapIn.getPropertyAsString("Name"));
        res.setModuleName(psSnapIn.getPropertyAsString("ModuleName"));
        res.setVersion( psSnapIn.getPropertyAsString("Version"));
        res.setDescription(psSnapIn.getPropertyAsString("Description"));
        return res;
    }

    public static List<CmdletInfo> getCmdletInfo(Session session, String cmdletName, String pssnapinName) {
        if (session == null) {
            throw new IllegalArgumentException("Parameter can not be null : " + session);
        }
        log.debug(String.format("getCmdletInfo : %s,%s",  cmdletName,pssnapinName));
        String cmdletFilter = "";
        if (cmdletName != null)
            cmdletFilter = "-CmdletName " + cmdletName;

        String snapinFilter = "";
        //if this is third party snapin we need to load it, in order to read the cmdletInfo
        if (!StringUtils.isBlank(pssnapinName) ) {
            snapinFilter = "-SnapinName " + pssnapinName;
            if (!isSystemSnapIn(pssnapinName)){
                session.invoke(String.format(ADD_PS_SNAPIN_S, pssnapinName));
            }
        }

        InvocationResult res = session.invoke(String.format(SCRIPT_GET_COMMAND_INFO, cmdletFilter, snapinFilter), CMDLET_SERIALIZATION_LEVEL);
        if (res.hasErrors()) {
            throw new PowerShellException("Unable to resolve Cmdlet definitions. " + res.getErrors().get(0));
        }

        try {
            return convertResultToCmdletList(res);
        } catch (XPathExpressionException e) {
            throw new PowerShellException("Unable to parse Cmdlet definitions. " + res.getErrors().get(0), e);
        }
    }

    private static List<CmdletInfo> convertResultToCmdletList(InvocationResult res) throws XPathExpressionException {
        log.debug("convertResultToCmdletList : Started");
        List<CmdletInfo> cmdlets = new ArrayList<CmdletInfo>();
        RemotePSObject results = res.getResults();
        if ( results == null ) {
            return cmdlets;
        } 
        
        Object root = results.getRootObject();
        
        if (root instanceof List<?>) {
            List<?> list = (List<?>) root;
            for (Object obj : list ) {
                cmdlets.add(convertResultToCmdlet(obj));
            }
        } else {
            cmdlets.add(convertResultToCmdlet(root));
        }
        log.debug("convertResultToCmdletList : Completed");
        return cmdlets;
    }

    private static CmdletInfo convertResultToCmdlet(Object obj) {
        
        assert(obj instanceof PSObject );
        CmdletInfo cmdlet = null;
        
        PSObject cmdletNode = ( PSObject )obj;
        
        cmdlet = new CmdletInfo( cmdletNode.getPropertyAsString("Name") );
        cmdlet.setCommandType(cmdletNode.getPropertyAsString("CommandType"));
        cmdlet.setPsSnapin(cmdletNode.getPropertyAsString("PSSnapIn"));
        //extract parameter sets names
        List<String> parameterSetsNames = new ArrayList<String>();
        for (Object item : cmdletNode.getPropertyAsPSObjectList("ParameterSets")) {
            parameterSetsNames .add( (String)item );
        }
        cmdlet.setDefinition(cmdletNode.getPropertyAsString("Definition"), parameterSetsNames.toArray(new String[0]));
        
        return cmdlet; 
    }

    private static Set<String> systemSnapIns = new HashSet<String>();
    static {
        //1.0
        systemSnapIns.add("Microsoft.PowerShell.Core");
        systemSnapIns.add("Microsoft.PowerShell.Host");
        systemSnapIns.add("Microsoft.PowerShell.Management");
        systemSnapIns.add("Microsoft.PowerShell.Security");
        systemSnapIns.add("Microsoft.PowerShell.Utility");
       //2.0
        systemSnapIns.add("Microsoft.PowerShell.Diagnostics");
        systemSnapIns.add("Microsoft.WSMan.Management");
    }

    public static boolean isSystemSnapIn(String pssnapinName) {
        return systemSnapIns.contains(pssnapinName);
    }

}
