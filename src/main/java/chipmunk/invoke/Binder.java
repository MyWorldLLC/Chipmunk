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

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Binder {

    protected final Class<?>[] callTypes;
    protected final Class<?>[] voidCallTypes;

    protected final CallCache cache;

    protected final MethodHandles.Lookup methodLookup;

    public Binder(){
        cache = new CallCache();

        callTypes = new Class<?>[11];
        callTypes[0] = Call.class;
        callTypes[1] = CallOne.class;
        callTypes[2] = CallTwo.class;
        callTypes[3] = CallThree.class;
        callTypes[4] = CallFour.class;
        callTypes[5] = CallFive.class;
        callTypes[6] = CallSix.class;
        callTypes[7] = CallSeven.class;
        callTypes[8] = CallEight.class;
        callTypes[9] = CallNine.class;
        callTypes[10] = CallTen.class;

        voidCallTypes = new Class<?>[11];
        voidCallTypes[0] = CallVoid.class;
        voidCallTypes[1] = CallOneVoid.class;
        voidCallTypes[2] = CallTwoVoid.class;
        voidCallTypes[3] = CallThreeVoid.class;
        voidCallTypes[4] = CallFourVoid.class;
        voidCallTypes[5] = CallFiveVoid.class;
        voidCallTypes[6] = CallSixVoid.class;
        voidCallTypes[7] = CallSevenVoid.class;
        voidCallTypes[8] = CallEightVoid.class;
        voidCallTypes[9] = CallNineVoid.class;
        voidCallTypes[10] = CallTenVoid.class;

        methodLookup = MethodHandles.lookup();
    }

    public Object lookupMethod(Object target, String opName, Class<?>[] paramTypes) throws Throwable {

        CallSignature signature = new CallSignature(target.getClass(), opName, paramTypes);
        Object callTarget = cache.getTarget(signature);
        if(callTarget != null){
            return callTarget;
        }

        Method[] methods = target.getClass().getMethods();
        Method method = null;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(opName)) {
                // only call public methods
                if (paramTypesMatch(methods[i].getParameterTypes(), paramTypes)
                        && ((methods[i].getModifiers() & Modifier.PUBLIC) != 0)) {

                    method = methods[i];
                    break;
                }
            }
        }

        if(method == null) {
            throw new NoSuchMethodException(formatMissingMethodMessage(target.getClass(), opName, paramTypes));
        }

        Class<?> callTypeClass = null;
        if(paramTypes.length < 11) {
            // direct-bind method

            if(method.getReturnType().equals(void.class)) {
                callTypeClass = this.voidCallTypes[paramTypes.length];
            }else {
                callTypeClass = this.callTypes[paramTypes.length];
            }

            try {
                MethodHandle implementationHandle = methodLookup.unreflect(method);

                MethodType interfaceType = MethodType.methodType(callTypeClass);
                MethodType implType = MethodType.methodType(method.getReturnType(), target.getClass()).appendParameterTypes(paramTypes);

                callTarget = LambdaMetafactory.metafactory(
                        methodLookup,
                        "call",
                        interfaceType,
                        implType.erase(),
                        implementationHandle,
                        implementationHandle.type())
                        .getTarget().invoke();
            } catch (IllegalAccessException | LambdaConversionException e) {
                throw e;
            }

        }else {
            // non-statically bind method
            try {
                callTarget = methodLookup.unreflect(method).asSpreader(1, Object[].class, paramTypes.length);
            } catch (IllegalAccessException e) {
                throw new NoSuchMethodException(formatMissingMethodMessage(target.getClass(), opName, paramTypes));
            }
        }

        cache.cacheTarget(signature, callTarget);
        return callTarget;
    }

    private boolean paramTypesMatch(Class<?>[] targetTypes, Class<?>[] callTypes) {

        if (targetTypes.length != callTypes.length) {
            return false;
        }

        for (int i = 0; i < targetTypes.length; i++) {
            if (targetTypes[i] != callTypes[i]) {
                if (!targetTypes[i].isAssignableFrom(callTypes[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    private String formatMissingMethodMessage(Class<?> targetType, String methodName, Class<?>[] paramTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append("No suitable method found: ");
        sb.append(targetType.getName());
        sb.append('.');
        sb.append(methodName);
        sb.append('(');

        for (int i = 0; i < paramTypes.length; i++) {
            sb.append(paramTypes[i].getName());
            if (i < paramTypes.length - 1) {
                sb.append(',');
            }
        }
        sb.append(')');
        return sb.toString();
    }

}
