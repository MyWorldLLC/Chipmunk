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

import chipmunk.runtime.NativeTypeLib;
import jdk.dynalink.*;
import jdk.dynalink.support.SimpleRelinkableCallSite;

import java.lang.invoke.*;

public class Binder {

    public static final String INDY_BOOTSTRAP_METHOD = "bootstrapCallsite";
    public static final String INDY_BOOTSTRAP_SET = "bootstrapSetSite";
    public static final String INDY_BOOTSTRAP_GET = "bootstrapGetSite";

    protected static final ChipmunkLinker chipmunkLinker = new ChipmunkLinker();
    protected static final DynamicLinker dynaLink = createDynamicLinker();

    public static CallSite bootstrapCallsite(MethodHandles.Lookup lookup, String name, MethodType callType) throws NoSuchMethodException, IllegalAccessException {
        return dynaLink.link(new SimpleRelinkableCallSite(
                new CallSiteDescriptor(lookup, chipmunkCallOp(name), callType)
        ));
    }

    public static CallSite bootstrapSetSite(MethodHandles.Lookup lookup, String name, MethodType callType) throws NoSuchMethodException, IllegalAccessException {
        return dynaLink.link(new SimpleRelinkableCallSite(
                new CallSiteDescriptor(lookup, chipmunkFieldSetOp(name), callType)
        ));
    }

    public static CallSite bootstrapGetSite(MethodHandles.Lookup lookup, String name, MethodType callType) throws NoSuchMethodException, IllegalAccessException {
        return dynaLink.link(new SimpleRelinkableCallSite(
                new CallSiteDescriptor(lookup, chipmunkFieldGetOp(name), callType)
        ));
    }

    protected static Operation chipmunkCallOp(String name){
        return StandardOperation.CALL.named(name);
    }

    protected static Operation chipmunkFieldSetOp(String name) {
        return StandardOperation.SET.named(name);
    }

    protected static Operation chipmunkFieldGetOp(String name) {
        return StandardOperation.GET.named(name);
    }

    public static MethodType bootstrapCallsiteType(){
        return MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    }

    public static MethodType bootstrapFieldOpType(){
        return MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    }

    protected static DynamicLinker createDynamicLinker(){
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        factory.setPrioritizedLinker(chipmunkLinker);

        return factory.createLinker();
    }

}
