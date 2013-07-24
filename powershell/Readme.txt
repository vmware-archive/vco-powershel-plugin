Copyright (c) 2011-2012 VMware, Inc.

This file is part of the vCO PowerShell Plug-in.

The vCO PowerShell Plug-in is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by the Free
Software Foundation version 3 and no later version.

The vCO PowerShell Plug-in is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version 3
for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
St, Fifth Floor, Boston, MA 02110-1301 USA.

=======================================
How to build the vCO PowerShell Plug-in
=======================================

Prerequisites:
- Java Development Kit 1.6+ installed and in your path
- Apache Ant 1.7.1+ installed and in your path
- Internet access to download the external dependencies

Steps:
1. Start a terminal
2. Go to the directory 'powershell'
3. Run the command 'ant'
4. The generated VMOAPP file is placed inside the directory 'dist'
5. You can install the VMOAPP file like any other plug-in in vCO (see vCO documentation)
