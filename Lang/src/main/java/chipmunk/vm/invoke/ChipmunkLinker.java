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

package chipmunk.vm.invoke;

import chipmunk.runtime.ChipmunkClass;
import chipmunk.runtime.ChipmunkObject;
import chipmunk.runtime.MethodBinding;
import chipmunk.runtime.TraitField;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.invoke.security.LinkingPolicy;
import jdk.dynalink.NamedOperation;
import jdk.dynalink.StandardOperation;
import jdk.dynalink.linker.GuardedInvocation;
import jdk.dynalink.linker.GuardingDynamicLinker;
import jdk.dynalink.linker.LinkRequest;
import jdk.dynalink.linker.LinkerServices;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ChipmunkLinker implements GuardingDynamicLinker {

    protected static final ThreadLocal<ChipmunkLibraries> libraries = new ThreadLocal<>();

    protected final MethodHandles.Lookup lookup;

    public ChipmunkLinker(){
        lookup = MethodHandles.lookup();
    }

    public static void setLibrariesForThread(ChipmunkLibraries libs){
        libraries.set(libs);
    }

    public static final Map<Class<?>, Class<?>> COMPATIBLE_PRIMITIVES = Map.ofEntries(
            Map.entry(Boolean.class, boolean.class),
            Map.entry(boolean.class, Boolean.class),
            Map.entry(Byte.class, byte.class),
            Map.entry(byte.class, Byte.class),
            Map.entry(Character.class, char.class),
            Map.entry(char.class, Character.class),
            Map.entry(Double.class, double.class),
            Map.entry(double.class, Double.class),
            Map.entry(Float.class, float.class),
            Map.entry(float.class, Float.class),
            Map.entry(Integer.class, int.class),
            Map.entry(int.class, Integer.class),
            Map.entry(Long.class, long.class),
            Map.entry(long.class, Long.class),
            Map.entry(Short.class, short.class),
            Map.entry(short.class, Short.class)
    );

    public static boolean isCallTypeCompatible(Class<?> expected, Class<?> test){
        if(expected.isAssignableFrom(test)){
            return true;
        }

        var compat = COMPATIBLE_PRIMITIVES.get(test);
        return compat != null && expected.isAssignableFrom(compat);
    }

    @Override
    public GuardedInvocation getGuardedInvocation(LinkRequest linkRequest, LinkerServices linkerServices) throws Exception {

        Object receiver = linkRequest.getReceiver();
        NamedOperation op = (NamedOperation) linkRequest.getCallSiteDescriptor().getOperation();
        MethodType callType = linkRequest.getCallSiteDescriptor().getMethodType();

        Object[] params = linkRequest.getArguments();

        if(op.getBaseOperation().equals(StandardOperation.CALL)){
            // Bind method calls
            return getInvocationHandle(lookup, receiver, callType, (String)op.getName(), params, true);
        }else if(op.getBaseOperation().equals(StandardOperation.GET)){
            // Bind field access
            Object target = linkRequest.getReceiver();
            Objects.requireNonNull(target, "Cannot access fields on a null reference");

            GuardedInvocation fieldHandle = getField(target, linkRequest, (String) op.getName(), false, true);
            if(fieldHandle == null){
                throw new NoSuchFieldException(target.getClass().getName() + "." + op.getName());
            }

            return fieldHandle;

        }else if( op.getBaseOperation().equals(StandardOperation.SET)){
            // Bind field set
            Object target = linkRequest.getReceiver();
            Objects.requireNonNull(target, "Cannot access fields on a null reference");

            GuardedInvocation fieldHandle = getField(target, linkRequest, (String) op.getName(), true, true);
            if(fieldHandle == null){
                throw new NoSuchFieldException(target.getClass().getName() + "." + op.getName());
            }

            return fieldHandle;
        }

        return null;
    }

    public GuardedInvocation getInvocationHandle(MethodHandles.Lookup lookup, Object receiver, String methodName, Object[] params) throws Exception {
        return getInvocationHandle(lookup, receiver, null, methodName, params, true);
    }

    public GuardedInvocation getInvocationHandle(MethodHandles.Lookup lookup, Object receiver, MethodType callType, String methodName, Object[] params) throws Exception {
        return getInvocationHandle(lookup, receiver, callType, methodName, params, true);
    }

    public GuardedInvocation getInvocationHandle(MethodHandles.Lookup lookup, Object receiver, MethodType callType, String methodName, Object[] params, boolean enforceLinkagePolicy) throws Exception {

        Class<?>[] pTypes = new Class<?>[params.length];
        for(int i = 0; i < params.length; i++){
            pTypes[i] = params[i] != null ? params[i].getClass() : null;
        }

        if(callType == null){
            callType = MethodType.methodType(Object.class, pTypes);
        }

        GuardedInvocation invocation = resolveCallTarget(lookup, receiver, callType, methodName, params, pTypes, enforceLinkagePolicy);

        if(invocation == null){
            // Failed to resolve method or a trait providing the method
            throw new NoSuchMethodException(
                    formatMethodSignature(receiver, methodName, pTypes));
        }

        return invocation;
    }

    public GuardedInvocation resolveCallTarget(MethodHandles.Lookup lookup, Object receiver, MethodType callType, String methodName, Object[] params, Class<?>[] pTypes, boolean enforceLinkagePolicy) throws Exception {
        // Library methods should override type methods, so check them first
        Class<?> expectedReturnType = callType.returnType();
        ChipmunkLibraries libs = getLibrariesForThread();
        MethodHandle callTarget = libs != null ? libs.getMethod(lookup, expectedReturnType, methodName, pTypes) : null;

        // TODO - need to be able to adapt the call type for variadic targets
        if (callTarget == null) {
            callTarget = getMethod(receiver, expectedReturnType, methodName, params, pTypes, enforceLinkagePolicy);
        }

        if (callTarget != null) {
            // Return non-trait invocation
            return new GuardedInvocation(callTarget, getCallGuard(lookup, callType, params));
        }

        // Check for trait methods
        TraitField[] traits = getTraitFields(receiver);

        if (traits != null) {
            for (TraitField trait : traits) {

                ReentrantLock traitLock = trait.getLock();
                try {
                    traitLock.lock();

                    Field receiverField = trait.getReflectedField();
                    if (receiverField == null) {
                        receiverField = receiver.getClass().getField(trait.getField());
                        receiverField.setAccessible(true);
                        trait.setReflectedField(receiverField);
                    }

                    Object traitReceiver = receiverField.get(receiver);
                    if (traitReceiver == null) {
                        continue;
                    }

                    GuardedInvocation invocation = resolveCallTarget(lookup, traitReceiver, callType, methodName, params, pTypes, enforceLinkagePolicy);

                    if (invocation != null) {

                        MethodHandle receiverFilter = lookup.unreflectGetter(trait.getReflectedField())
                                .asType(MethodType.methodType(traitReceiver.getClass(), receiver.getClass()));

                        invocation = invocation
                                .addSwitchPoint(trait.getInvalidationPoint())
                                .replaceMethods(
                                        MethodHandles.filterArguments(invocation.getInvocation(), 0, receiverFilter),
                                        invocation.getGuard()
                                );

                        return invocation;
                    }

                } finally {
                    traitLock.unlock();
                }

            }
        }

        return null;
    }

    public static void linkOrder(Method[] methods){
        Arrays.sort(methods, (m1, m2) -> {
            // Favor shorter parameter lists first
            if(m1.getParameterCount() < m2.getParameterCount()){
                return -1;
            }

            // Favor simple types over interfaces (which will be proxied to). This
            // ensures that interface linkage only happens if a more appropriate linkage
            // is not found first. This is important because without type information,
            // interface receiver types will match with any parameter, resulting in some
            // cases where interface proxying happens when a different method is available
            // with an identical type match. Worse, these scenarios are unpredictable since
            // the order of methods returned by Object.getMethods() is not specified.
            if(m1.getParameterCount() == m2.getParameterCount()){
                var p1Types = m1.getParameterTypes();
                var p2Types = m2.getParameterTypes();
                int m1Weight = 0;
                for(int i = 0; i < p1Types.length; i++){
                    var p1IsInterface = p1Types[i].isInterface();
                    var p2IsInterface = p2Types[i].isInterface();

                    if(p1IsInterface && !p2IsInterface){
                        m1Weight++;
                    }else if(!p1IsInterface && p2IsInterface){
                        m1Weight--;
                    }
                }
                return m1Weight;
            }

            // At this point we know m2 has a shorter parameter list
            return 1;
        });
    }

    public MethodHandle getMethod(Object receiver, Class<?> expectedReturnType, String methodName, Object[] params, Class<?>[] pTypes, boolean enforceLinkagePolicy) throws IllegalAccessException, NoSuchMethodException {

        Class<?> receiverType;
        if(receiver == null){
            receiverType = Object.class;
        }else if(receiver instanceof Class){
            receiverType = (Class<?>) receiver;
        }else{
            receiverType = receiver.getClass();
        }

        var methods = receiverType.getMethods();
        linkOrder(methods);
        for (Method m : methods) {
            Class<?>[] candidatePTypes = m.getParameterTypes();

            if (candidatePTypes.length != pTypes.length - 1 && !m.isVarArgs()) {
                continue;
            }

            if (!getMethodName(m).equals(methodName)) {
                continue;
            }

            Class<?> retType = m.getReturnType();

            if (retType.equals(void.class) || isCallTypeCompatible(expectedReturnType, retType)) {

                boolean paramsMatch = true;
                long interfaceParamMask = 0;
                boolean isStatic = Modifier.isStatic(m.getModifiers());
                for (int i = 0; i < candidatePTypes.length; i++) {

                    // Need to offset by 1 because the incoming types include the receiver type
                    // while the candidate types do not
                    Class<?> callPType = pTypes[i + 1];
                    Class<?> candidatePType = candidatePTypes[i];

                    //isCallTypeCompatible(candidatePType, callPType != null ? callPType : Object.class);
                    if (!isCallTypeCompatible(candidatePType, callPType != null ? callPType : Object.class)) {
                        if(candidatePType.isInterface()){
                            interfaceParamMask |= (1L << i);
                            continue;
                        }
                        paramsMatch = false;
                        break;
                    }
                }

                if (paramsMatch || m.isVarArgs()) {
                    // We have a match!
                    LinkingPolicy linkPolicy = getLinkingPolicy();
                    if(linkPolicy != null && enforceLinkagePolicy && !linkPolicy.allowMethodCall(receiver, m, params)){
                        throw new IllegalAccessException(formatMethodSignature(receiver, methodName, pTypes) + ": policy forbids call");
                    }
                    m.setAccessible(true);
                    var handle = isStatic
                            ? MethodHandles.dropArguments(lookup.unreflect(m), 0, Object.class)
                            : lookup.unreflect(m);
                    while(interfaceParamMask != 0){
                        for(int i = candidatePTypes.length - 1; i >= 0; i--){
                            var paramIndex = isStatic ? i : i + 1;
                            if((interfaceParamMask & 1) != 0){
                                handle = MethodHandles.filterArguments(handle, paramIndex, ProxyFilter.filterFor(lookup, candidatePTypes[i]).asType(MethodType.methodType(candidatePTypes[i], Object.class)));
                            }
                            interfaceParamMask >>>= 1; // Do unsigned right shift so that a 1 in the leading bit isn't propagated
                        }
                    }
                    if(m.isVarArgs()){
                        handle = handle.asVarargsCollector(Object[].class);
                    }
                    return handle;
                }

            }
        }

        return null;
    }

    public GuardedInvocation getField(Object receiver, LinkRequest linkRequest, String fieldName, boolean set, boolean enforceLinkagePolicy) throws Exception {

        Class<?> receiverType;
        if(receiver == null){
            receiverType = Object.class;
        }else if(receiver instanceof Class){
            receiverType = (Class<?>) receiver;
        }else{
            receiverType = receiver.getClass();
        }

        Field[] fields = receiverType.getFields();
        for(Field f : fields){
            if(getFieldName(f).equals(fieldName)){

                LinkingPolicy linkPolicy = getLinkingPolicy();
                if(linkPolicy != null && enforceLinkagePolicy){
                    if(set){
                        if(!linkPolicy.allowFieldSet(receiver, f, linkRequest.getArguments()[1])){
                            throw new IllegalAccessException(receiverType.getName() + "." + fieldName + ": policy forbids set to " + linkRequest.getArguments()[1]);
                        }
                    }else {
                        if(!linkPolicy.allowFieldGet(receiver, f)){
                            throw new IllegalAccessException(receiver.getClass().getName() + "." + fieldName + ": policy forbids get");
                        }
                    }

                }


                MethodHandle accessor = lookup.unreflectVarHandle(f)
                        .toMethodHandle(set ? VarHandle.AccessMode.SET : VarHandle.AccessMode.GET);
                
                if(Modifier.isStatic(f.getModifiers())){
                    accessor = MethodHandles.dropArguments(accessor, 0, Object.class);
                }

                // If this field is a trait and we're setting, invalidate the trait switch point
                if(set){
                    TraitField trait = getTraitField(receiver, fieldName);
                    if(trait != null){
                        // If setting, need to invalidate the field's switchpoint. Implementing this
                        // as a bound method filter lets us easily inline this into the handle call sequence.
                        MethodHandle invalidationFilter = lookup.findStatic(
                                this.getClass(),
                                "invalidateTraitField",
                                MethodType.methodType(Object.class, TraitField.class, Object.class)
                        ).bindTo(trait)
                                .asType(MethodType.methodType(receiverType, receiverType));

                        accessor = MethodHandles.filterArguments(accessor, 0, invalidationFilter);
                    }
                }

                return new GuardedInvocation(accessor, getFieldGuard(lookup, receiver));
            }
        }

        TraitField[] traitFields = getTraitFields(receiver);

        if(traitFields == null){
            return null;
        }

        for(TraitField trait : traitFields){
            ReentrantLock traitLock = trait.getLock();
            try {
                traitLock.lock();

                Field receiverField = trait.getReflectedField();
                if (receiverField == null) {
                    for(Field f : receiverType.getFields()){
                        if(getFieldName(f).equals(trait.getField())){
                            receiverField = f;
                            receiverField.setAccessible(true);
                            trait.setReflectedField(receiverField);
                            break;
                        }
                    }
                }

                if(receiverField == null){
                    continue;
                }

                Object traitReceiver = receiverField.get(receiver);
                if (traitReceiver == null) {
                    continue;
                }

                GuardedInvocation invocation = getField(traitReceiver, linkRequest, fieldName, set, true);
                if(invocation != null){
                    Class<?> rootReceiverType = linkRequest.getReceiver().getClass();

                    MethodType filterType = MethodType.methodType(traitReceiver.getClass(), receiver.getClass());

                    MethodHandle receiverFilter = lookup.unreflectGetter(trait.getReflectedField())
                            .asType(filterType);

                    if(set){
                        // If setting, need to invalidate the field's switchpoint. Implementing this
                        // as a bound method filter lets us easily inline this into the handle call sequence.
                        MethodHandle invalidationFilter = lookup.findStatic(
                                this.getClass(),
                                "invalidateTraitField",
                                MethodType.methodType(Object.class, TraitField.class, Object.class)
                        ).bindTo(trait)
                                .asType(MethodType.methodType(receiver.getClass(), receiver.getClass()));

                        receiverFilter = MethodHandles.filterArguments(receiverFilter, 0, invalidationFilter);
                    }

                    MethodHandle invoker = MethodHandles.filterArguments(invocation.getInvocation(), 0, receiverFilter);

                    invocation = invocation.addSwitchPoint(trait.getInvalidationPoint())
                            .replaceMethods(invoker, getFieldGuard(lookup, receiver));

                    return invocation;
                }

            }finally{
                traitLock.unlock();
            }
        }

        return null;
    }

    public static boolean validateCall(Object[] boundArgs, Object[] callArgs){
        if(boundArgs.length != callArgs.length){
            return false;
        }

        for(int i = 0; i < boundArgs.length; i++){
            Class<?> boundType = boundArgs[i] != null ? boundArgs[i].getClass() : null;
            Class<?> callType = callArgs[i] != null ? callArgs[i].getClass() : null;

            if(boundType != callType){
                return false;
            }
        }
        return true;
    }

    public static boolean validateFieldAccess(Object boundTarget, Object fieldValue){
        if(fieldValue == null){
            return true;
        }
        return boundTarget.getClass() == fieldValue.getClass();
    }

    public static Object invalidateTraitField(TraitField f, Object target) {
        f.invalidateAndReset();
        return target;
    }

    protected MethodHandle getCallGuard(MethodHandles.Lookup lookup, MethodType callType, Object[] params) throws NoSuchMethodException, IllegalAccessException {
        MethodType guardType = callType.generic().changeReturnType(Boolean.class);

        return lookup.findStatic(
                this.getClass(),
                "validateCall",
                MethodType.methodType(boolean.class, Object[].class, Object[].class))
                .bindTo(params)
                .asCollector(Object[].class, guardType.parameterCount());
    }

    protected MethodHandle getFieldGuard(MethodHandles.Lookup lookup, Object target) throws NoSuchMethodException, IllegalAccessException {
        return lookup.findStatic(
                this.getClass(),
                "validateFieldAccess",
                MethodType.methodType(boolean.class, Object.class, Object.class))
                .bindTo(target);
    }

    public ChipmunkLibraries getLibrariesForThread(){
        return libraries.get();
    }

    protected LinkingPolicy getLinkingPolicy(){
        ChipmunkScript script = ChipmunkScript.getCurrentScript();
        if(script == null){
            return null;
        }

        return script.getLinkPolicy();
    }

    protected TraitField[] getTraitFields(Object target){

        TraitField[] traitFields = null;

        if(target instanceof ChipmunkObject){
            traitFields = ((ChipmunkObject) target).getChipmunkClass().getTraits();
        }else if(target instanceof ChipmunkClass){
            traitFields = ((ChipmunkClass) target).getSharedTraits();
        }

        return traitFields;
    }

    protected TraitField getTraitField(Object target, String field){
        TraitField[] traitFields = getTraitFields(target);
        if(traitFields == null){
            return null;
        }

        for(TraitField f : traitFields){
            if(f.getField().equals(field)){
                return f;
            }
        }

        return null;
    }

    protected String getFieldName(Field f){
        ChipmunkName override = f.getAnnotation(ChipmunkName.class);
        return override != null ? override.value() : f.getName();
    }

    protected String getMethodName(Method m){
        ChipmunkName override = m.getAnnotation(ChipmunkName.class);
        return override != null ? override.value() : m.getName();
    }

    public String formatMethodSignature(Object receiver, String methodName, Class<?>[] pTypes){
        String receiverName;
        if(receiver == null){
            receiverName = "null";
        }else if(receiver instanceof Class){
            receiverName = ((Class<?>) receiver).getName();
        }else{
            receiverName = receiver.getClass().getName();
        }
        return receiverName + "." + methodName + "(" +
                Arrays.stream(pTypes)
                        .skip(1) // Skip self reference
                        .map(c -> c != null ? c.getName() : "null")
                        .collect(Collectors.joining(","))
                + ")";
    }

}
