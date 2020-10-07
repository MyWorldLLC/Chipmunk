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

package chipmunk.invoke;

import java.util.Arrays;
import java.util.Objects;

public class CallSignature {

    protected final Class<?> targetType;
    protected final String methodName;
    protected final Class<?>[] paramTypes;

    public CallSignature(Class<?> targetType, String methodName, Class<?>[] paramTypes){
        this.targetType = targetType;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
    }

    public static CallSignature makeSignature(Object target, String methodName, Object[] params){
        Class<?> targetType = target.getClass();
        Class<?>[] paramTypes = new Class<?>[params != null ? params.length : 0];

        for(int i = 0; i < paramTypes.length; i++){
            paramTypes[i] = params[i] != null ? params[i].getClass() : null;
        }

        return new CallSignature(targetType, methodName, paramTypes);
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallSignature that = (CallSignature) o;
        return Objects.equals(targetType, that.targetType) &&
                Objects.equals(methodName, that.methodName) &&
                Arrays.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(targetType, methodName);
        result = 31 * result + Arrays.hashCode(paramTypes);
        return result;
    }
}
