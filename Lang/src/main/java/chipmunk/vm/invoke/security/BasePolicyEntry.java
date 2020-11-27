/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.vm.invoke.security;

public class BasePolicyEntry implements PolicyEntry {

    protected final SecurityMode methodMode;
    protected final SecurityMode fieldSetMode;
    protected final SecurityMode fieldGetMode;

    public BasePolicyEntry(){
        this(SecurityMode.DENYING);
    }

    public BasePolicyEntry(SecurityMode mode){
        this(mode, mode, mode);
    }

    public BasePolicyEntry(SecurityMode methodMode, SecurityMode fieldSetMode, SecurityMode fieldGetMode){
        this.methodMode = methodMode;
        this.fieldSetMode = fieldSetMode;
        this.fieldGetMode = fieldGetMode;
    }

    public SecurityMode getMethodMode(){
        return methodMode;
    }

    public SecurityMode getFieldSetMode(){
        return fieldSetMode;
    }

    public SecurityMode getFieldGetMode(){
        return fieldGetMode;
    }

    protected AccessEvaluation evaluateMethodAccess(boolean specifiedByPolicy){
        return evaluateAccess(methodMode, specifiedByPolicy);
    }

    protected AccessEvaluation evaluateFieldSetAccess(boolean specifiedByPolicy){
        return evaluateAccess(fieldSetMode, specifiedByPolicy);
    }

    protected AccessEvaluation evaluateFieldGetAccess(boolean specifiedByPolicy){
        return evaluateAccess(fieldGetMode, specifiedByPolicy);
    }

    protected AccessEvaluation evaluateAccess(SecurityMode mode, boolean specifiedByPolicy){
        if(!specifiedByPolicy){
            return mode == SecurityMode.ALLOWING ? AccessEvaluation.DENIED : AccessEvaluation.ALLOWED;
        }

        if(mode == SecurityMode.ALLOWING){
            return AccessEvaluation.ALLOWED;
        }
        return AccessEvaluation.DENIED;
    }
}
