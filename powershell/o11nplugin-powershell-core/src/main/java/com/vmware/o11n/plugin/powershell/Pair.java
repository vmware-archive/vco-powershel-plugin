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

package com.vmware.o11n.plugin.powershell;

/** 
 * An utility container for a pair of objects
 */
public class Pair<F, S> {
    private F first;
    private S second;

    /**
     * Constructor.
     * 
     * @param first
     * A value for the first component of the pair.
     * @param second
     * A value for the second component of the pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Retrieve the value of the first component in the pair.
     * 
     * @return
     * The value.
     */
    public F getFirst() {
        return first;
    }

    /**
     * Set the value of the first component in the pair.
     * 
     * @param first
     * The value to be set.
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     * Retrieve the value of the second component in the pair.
     * 
     * @return
     * The value.
     */
    public S getSecond() {
        return second;
    }

    /**
     * Set the value of the second component in the pair.
     * 
     * @param second
     * The value to be set.
     */
    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("rawtypes")
        Pair other = (Pair) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(first);
        sb.append(", ");
        sb.append(second);
        sb.append("]");
        return sb.toString();
    }
}
