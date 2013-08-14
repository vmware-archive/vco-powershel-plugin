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

package com.vmware.o11n.plugin;

import com.vmware.o11n.plugin.powershell.Constants;
import com.vmware.o11n.plugin.sdk.module.ModuleBuilder;

public final class PowerShellModuleBuilder extends ModuleBuilder {

	private static final String DESCRIPTION = "PowerShell plug-in for vCenter Orchestrator";

	private static final String DATASOURCE = "main-datasource";

	@Override
	public void configure() {
		module(Constants.PLUGIN_NAME).displayName(Constants.PLUGIN_NAME)
				.withDescription("Power Shell Plug-in")
				.withImage("images/power-shell-32x32.PNG")
				.basePackages("com.vmware.o11n.plugin.powershell")
				.version(
                        "1.0.2");

		InstallationBuilder installation = installation(InstallationMode.BUILD);
        installation.action(ActionType.INSTALL_PACKAGE,	"packages/o11nplugin-powershell-package-1.0.2-48.package");
		installation.action(ActionType.INSTALL_PACKAGE,   "packages/o11nplugin-powershell-package-converter-1.0.2-48.package");

		inventory(Constants.FINDER_POWER_SHELL_PLUGIN);

		finderDatasource(PowerShellPluginAdaptor.class, DATASOURCE)
				.anonymousLogin(LoginMode.INTERNAL);

		this.finder(Constants.FINDER_POWER_SHELL_PLUGIN, DATASOURCE).addRelation(Constants.FINDER_POWER_SHELL_HOST, Constants.RELATION_POWER_SHELL_HOST);

	}
}
