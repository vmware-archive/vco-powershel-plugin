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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.o11n.plugin.powershell.Pair;

public class UserProfileData {
    //True only if info for all snapins is loaded  
    private boolean  snapinInfoLoaded = false;
    List<SnapInInfo> snapins;
    
    // Snapin -> CmdletInfo
    Map< String,  Pair<Boolean, List<CmdletInfo>>> cmdlets;
    private String userName;
    
    public UserProfileData( String userName){
        this.snapins = new ArrayList<SnapInInfo>();
        this.cmdlets = new HashMap< String,  Pair<Boolean, List<CmdletInfo>>>();
        this.setUserName(userName);
    }
    
    public List<SnapInInfo> getSnapIns() {
        synchronized (snapins) {
            if (snapinInfoLoaded ){
                return snapins;
            }
        }        
        return null;
    }
    
    public SnapInInfo getSnapIn(String name) {
        
        SnapInInfo snapin = null;
        synchronized (snapins) {
            //try if we have already loaded it
            for( SnapInInfo info : snapins){
                if ( info.getName().equalsIgnoreCase(name) ){
                    return info;
                } 
            }
        }
        
        return snapin;
    }

    public void preloadSnapinsInfo(List<SnapInInfo> snapInInfos) {
        synchronized (snapins) {
            this.snapins = snapInInfos;
            this.snapinInfoLoaded = true;
        }
    }

    public void register(SnapInInfo snapin) {
        if (snapin == null ){
            return;
        }

        synchronized (snapins) {
            this.snapins.add(snapin);    
        }
    }

    public List<CmdletInfo> getCmdlets(String  snapinName) {
        synchronized (cmdlets) {
            Pair<Boolean, List<CmdletInfo>> entry = cmdlets.get(snapinName);
            if (entry != null && entry.getFirst()){
                return entry.getSecond();
            }
        }        
        return null;
    }
    
    public CmdletInfo getCmdlet(String cmdletName, String snapinName) {
        CmdletInfo snapin = null;
        synchronized (cmdlets) {
            Pair<Boolean, List<CmdletInfo>> entry = cmdlets.get(snapinName);
            if ( entry != null ){
                //try if we have already loaded it
                for( CmdletInfo info : entry.getSecond()){
                    if ( info.getName().equalsIgnoreCase(cmdletName) && info.getPsSnapin().equalsIgnoreCase(snapinName) ){
                        return info;
                    } 
                }
            }
        }
        
        return snapin;
    }

    public void register(CmdletInfo cmdletInfo) {
        if (cmdletInfo == null ){
            return;
        }
        synchronized (cmdlets) {
            Pair<Boolean, List<CmdletInfo>> entry = cmdlets.get(cmdletInfo.getPsSnapin());
            if ( entry == null ){
                entry = new Pair<Boolean, List<CmdletInfo>>(false,new ArrayList<CmdletInfo>());
                this.cmdlets.put(cmdletInfo.getPsSnapin(),entry);
            }
            entry.getSecond().add(cmdletInfo);
        }
    }

    public void preloadCmdletsInfo(List<CmdletInfo> newCmdlets, String snapinName) {
        synchronized (cmdlets) {
            Pair<Boolean, List<CmdletInfo>> entry = this.cmdlets.get(snapinName);
            if ( entry == null ){
                entry = new Pair<Boolean, List<CmdletInfo>>(true,new ArrayList<CmdletInfo>());
                this.cmdlets.put(snapinName,entry);
            } else {
                entry.getSecond().clear();
            }
            
            entry.getSecond().addAll(newCmdlets);
        }
    }

    public void invalidateSnapin(String snapIn) {
        cmdlets.remove(snapIn);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
