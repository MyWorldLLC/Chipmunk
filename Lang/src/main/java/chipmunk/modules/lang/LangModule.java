/*
 * Copyright (C) 2021 MyWorld, LLC
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

package chipmunk.modules.lang;

import chipmunk.runtime.*;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.binding.NativeBinding;
import chipmunk.vm.invoke.ChipmunkName;
import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.util.*;

public class LangModule implements NativeModule {

    public static final String MODULE_NAME = "chipmunk.lang";

    @AllowChipmunkLinkage
    @ChipmunkName("Any")
    public final Class<Object> _any;

    @AllowChipmunkLinkage
    @ChipmunkName("Int")
    public final Class<Integer> _int;

    @AllowChipmunkLinkage
    @ChipmunkName("Float")
    public final Class<Float> _float;

    @AllowChipmunkLinkage
    @ChipmunkName("Boolean")
    public final Class<Boolean> _boolean;

    @AllowChipmunkLinkage
    @ChipmunkName("String")
    public final Class<String> _string;

    @AllowChipmunkLinkage
    @ChipmunkName("List")
    public final Class<List> _list;

    @AllowChipmunkLinkage
    @ChipmunkName("Map")
    public final Class<Map> _map;

    public LangModule(){
        _any = Object.class;
        _int = Integer.class;
        _float = Float.class;
        _boolean = Boolean.class;
        _string = String.class;

        _list = List.class;
        _map = Map.class;
    }

    @AllowChipmunkLinkage
    public void unimplementedMethod() throws UnimplementedMethodException {
        throw new UnimplementedMethodException();
    }

    /*@AllowChipmunkLinkage
    public MethodBinding bindArgs(MethodBinding binding, Integer index, List<Object> args) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return ChipmunkScript.getCurrentScript().getVM().bindArgs(binding, index, args.toArray());
    }*/

    @AllowChipmunkLinkage
    public ChipmunkModule getModule(String name){
        return ChipmunkScript.getCurrentScript()
                .getHazelVM()
                .getModule(name);
    }

    @Override
    public String getName(){
        return MODULE_NAME;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerTypeBindings(NativeBinding binding) {

        binding.register(CClass.class, builder -> {
            builder.withNativeMethod("getModule", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, 1, fiber.vm().fromHostValue(((CClass) target).module()));
                return ip + 1;
            }));
        });

        binding.register(double[].class, builder -> {
            builder.withNativeMethod("getModule", ((fiber, ip, bp, sp, argCount, target) -> {
                var cls = (CClass) fiber.vm().heap().read(((double[]) target)[0]);
                fiber.pushResult(bp, sp, 1, fiber.vm().fromHostValue(cls.module()));
                return ip + 1;
            }));
        });


        binding.register(ArrayList.class, builder -> {

            builder.withNativeMethod("getAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = (int) fiber.readArg(bp, sp, 2, 1);
                var value = ((ArrayList) target).get(index);
                fiber.pushResult(bp, sp, 2, fiber.vm().fromHostValue(value));
                return ip + 1;
            }));

            builder.withNativeMethod("setAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = (int) fiber.readArg(bp, sp, 3, 1);
                var prior = ((ArrayList) target).set(index, fiber.vm().toHostValue(fiber.readArg(bp, sp, 3, 2)));
                fiber.pushResult(bp, sp, 3, fiber.vm().fromHostValue(prior));
                return ip + 1;
            }));

            builder.withNativeMethod("sort", ((fiber, ip, bp, sp, argCount, target) -> {
                ((ArrayList) target).sort(Comparator.naturalOrder());
                fiber.pushResult(bp, sp, 1, fiber.readArg(bp, sp, 1, 0)); // Return self as the result
                return ip + 1;
            }));

            builder.withNativeMethod("iterator", ((fiber, ip, bp, sp, argCount, target) -> {
                var it = new CListIterator((List)fiber.vm().toHostValue(fiber.readArg(bp, sp, 1, 0)));
                fiber.pushResult(bp, sp, 1, fiber.vm().fromHostValue(it));
                return ip + 1;
            }));

        });

        binding.register(CListIterator.class, builder -> {
           builder.withNativeMethod("hasNext", ((fiber, ip, bp, sp, argCount, target) -> {
               fiber.pushResult(bp, sp, 1, ((CListIterator) target).hasNext() ? 1 : 0);
               return ip + 1;
           }));

            builder.withNativeMethod("next", ((fiber, ip, bp, sp, argCount, target) -> {
                // TODO - this here demonstrates that double[] as the object format is insufficient due to lack of a stable
                // self pointer. Passing a Chipmunk object instance back from here would result in it being re-allocated under
                // a different pointer, meaning multiple pointers could end up aliasing the same underlying value. This will
                // cause issues with the 'is' operator, among other possible problems. We need a 'CObject' class with a stable self pointer.
                // Stashing the self pointer in the array would significantly complicate various parts of the VM, and would probably have
                // nearly as much memory overhead as a wrapper object.
                fiber.pushResult(bp, sp, 1, fiber.vm().fromHostValue(((CListIterator) target).next()));
                return ip + 1;
            }));
        });

        binding.register(HashMap.class, builder -> {

            builder.withNativeMethod("getAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = fiber.vm().toHostValue(fiber.readArg(bp, sp, 2, 1));
                var value = ((HashMap) target).get(index);
                fiber.pushResult(bp, sp, 2, fiber.vm().fromHostValue(value));
                return ip + 1;
            }));

            builder.withNativeMethod("setAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = fiber.vm().toHostValue(fiber.readArg(bp, sp, 3, 1));
                var prior = ((HashMap) target).put(index, fiber.vm().toHostValue(fiber.readArg(bp, sp, 3, 2)));
                fiber.pushResult(bp, sp, 3, fiber.vm().fromHostValue(prior));
                return ip + 1;
            }));

        });

        binding.register(String.class, builder -> {
            builder.withNativeMethod("plus", ((fiber, ip, bp, sp, argCount, target) -> {
                var v = fiber.readArg(bp, sp, 2, 1);
                var other = Value.isNumber(v) ? Double.toString(v) : Objects.toString(fiber.vm().heap().read(v));
                fiber.pushResult(bp, sp, 2, fiber.vm().fromHostValue(((String) target).concat(other)));
                return ip + 1;
            }));
        });

        binding.register(CMethodBinding.class, builder -> {
            builder.withNativeMethod("call", ((fiber, ip, bp, sp, argCount, target) -> {
                var methodBinding = (CMethodBinding) target;

                var totalArgs = argCount;
                var boundArgs = methodBinding.args();
                if(boundArgs != null){
                    totalArgs += boundArgs.length;
                    // Bulk copy the bound args to the stack
                    System.arraycopy(boundArgs, 0, fiber.stack, bp + sp - totalArgs + 1, boundArgs.length);
                }
                // Overwrite self
                fiber.pushResult(bp, sp, totalArgs, methodBinding.target());
                return methodBinding.dynamicCall(fiber, ip, bp, sp, methodBinding.methodName(), totalArgs);
            }));

            builder.withNativeMethod("bindArgs", ((fiber, ip, bp, sp, argCount, target) -> {
                var methodBinding = (CMethodBinding) target;
                // TODO - this doesn't implement the same API as prior versions did.
                var captured = argCount - 1; // don't capture self with the args
                var args = new double[captured];
                System.arraycopy(fiber.stack, bp + sp - captured, args, 0, captured);
                methodBinding.bindArgs(args);
                return ip + 1;
            }));
        });
    }
}
