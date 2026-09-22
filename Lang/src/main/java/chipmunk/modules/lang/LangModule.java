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

import chipmunk.ChipmunkException;
import chipmunk.runtime.*;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.hazel.TypeError;
import chipmunk.vm.hazel.Value;
import chipmunk.vm.hazel.invoke.binding.NativeBinding;
import chipmunk.vm.invoke.AllowChipmunkLinkage;

import java.util.*;

public class LangModule implements NativeModule {

    public static final String MODULE_NAME = "chipmunk.lang";

    @AllowChipmunkLinkage
    public final String Boolean = "Boolean";

    @AllowChipmunkLinkage
    public final String String = "String";

    @AllowChipmunkLinkage
    public final String List = "List";

    @AllowChipmunkLinkage
    public final String Map = "Map";

    @AllowChipmunkLinkage
    public void unimplementedMethod() throws UnimplementedMethodException {
        throw new UnimplementedMethodException();
    }

    /*@AllowChipmunkLinkage
    public ChipmunkModule getModule(String name){
        return ChipmunkScript.getCurrentScript()
                .getHazelVM()
                .getModule(name);
    }*/

    @Override
    public String getName(){
        return MODULE_NAME;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerTypeBindings(NativeBinding binding) {

        binding.register(CClass.class, builder -> {
            builder.withNativeMethod("getModule", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(((CClass) target).module()));
                return ip + 1;
            }));
        });

        binding.register(CObject.class, builder -> {
            builder.withNativeMethod("getModule", ((fiber, ip, bp, sp, argCount, target) -> {
                var cls = (CClass) fiber.vm().heap().read(((CObject) target).storage()[0]);
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(cls.module()));
                return ip + 1;
            }));
        });


        binding.register(CList.class, builder -> {

            builder.withNativeMethod("add", ((fiber, ip, bp, sp, argCount, target) -> {
                var value = fiber.readArg(bp, sp, argCount, 1);
                ((CList) target).add(value);
                fiber.pushResult(bp, sp, argCount, value);
                return ip + 1;
            }));

            builder.withNativeMethod("getAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = (int) fiber.readArg(bp, sp, argCount, 1);
                var value = ((CList) target).get(index);
                fiber.pushResult(bp, sp, argCount, value);
                return ip + 1;
            }));

            builder.withNativeMethod("setAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = (int) fiber.readArg(bp, sp, argCount, 1);
                var prior = ((CList) target).set(index, fiber.readArg(bp, sp, argCount, 2));
                fiber.pushResult(bp, sp, argCount, prior);
                return ip + 1;
            }));

            builder.withNativeMethod("remove", ((fiber, ip, bp, sp, argCount, target) -> {
                var index = (int) fiber.readArg(bp, sp, argCount, 1);
                var prior = ((CList) target).remove(index);
                fiber.pushResult(bp, sp, argCount, prior);
                return ip + 1;
            }));

            builder.withNativeMethod("sort", ((fiber, ip, bp, sp, argCount, target) -> {
                if(argCount == 2){
                    try{
                        ((CList) target).sort((CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 1)));
                        fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // Return self as the result
                    } catch (Exception e) {
                        throw new ChipmunkException(fiber, e.getMessage(), e);
                    }
                }else{
                    ((CList) target).sort();
                    fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // Return self as the result
                }
                return ip + 1;
            }));

            builder.withNativeMethod("has", ((fiber, ip, bp, sp, argCount, target) -> {
                var value = fiber.readArg(bp, sp, argCount, 1);
                var has = ((CList) target).contains(value);
                fiber.pushResult(bp, sp, argCount, has ? 1 : 0);
                return ip + 1;
            }));

            builder.withNativeMethod("iterator", ((fiber, ip, bp, sp, argCount, target) -> {
                var it = new CListIterator((CList)fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0)));
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(it));
                return ip + 1;
            }));

            builder.withNativeMethod("range", ((fiber, ip, bp, sp, argCount, target) -> {
                var list = (CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0));
                var range = (CRange) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 1));
                var it = new CListRangeIterator(list, range.iterator());
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(it));
                return ip + 1;
            }));

            builder.withNativeMethod("compact", ((fiber, ip, bp, sp, argCount, target) -> {
                ((CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).compact();
                fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // return self
                return ip + 1;
            }));

            builder.withNativeMethod("clear", ((fiber, ip, bp, sp, argCount, target) -> {
                ((CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).clear();
                fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // return self
                return ip + 1;
            }));

            builder.withNativeMethod("size", ((fiber, ip, bp, sp, argCount, target) -> {
                var size = ((CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).size();
                fiber.pushResult(bp, sp, argCount, size);
                return ip + 1;
            }));

        });

        binding.register(CListIterator.class, builder -> {
           builder.withNativeMethod("hasNext", ((fiber, ip, bp, sp, argCount, target) -> {
               fiber.pushResult(bp, sp, argCount, ((CListIterator) target).hasNext() ? 1 : 0);
               return ip + 1;
           }));

            builder.withNativeMethod("next", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, ((CListIterator) target).next());
                return ip + 1;
            }));
        });

        binding.register(CListRangeIterator.class, builder -> {
            builder.withNativeMethod("hasNext", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, ((CListRangeIterator) target).hasNext());
                return ip + 1;
            }));

            builder.withNativeMethod("next", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, ((CListRangeIterator) target).next());
                return ip + 1;
            }));
        });

        binding.register(CMap.class, builder -> {

            builder.withNativeMethod("getAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var key = fiber.readArg(bp, sp, argCount, 1);
                var value = ((CMap) target).get(key);
                fiber.pushResult(bp, sp, argCount, value);
                return ip + 1;
            }));

            builder.withNativeMethod("setAt", ((fiber, ip, bp, sp, argCount, target) -> {
                var key = fiber.readArg(bp, sp, argCount, 1);
                var prior = ((CMap) target).insert(key, fiber.readArg(bp, sp, argCount, 2));
                fiber.pushResult(bp, sp, argCount, prior);
                return ip + 1;
            }));

            builder.withNativeMethod("has", ((fiber, ip, bp, sp, argCount, target) -> {
                var key = fiber.readArg(bp, sp, argCount, 1);
                var value = ((CMap) target).contains(key);
                fiber.pushResult(bp, sp, argCount, value ? 1 : 0);
                return ip + 1;
            }));

            builder.withNativeMethod("remove", ((fiber, ip, bp, sp, argCount, target) -> {
                var key = (int) fiber.readArg(bp, sp, argCount, 1);
                var prior = ((CMap) target).remove(key);
                fiber.pushResult(bp, sp, argCount, prior);
                return ip + 1;
            }));

            builder.withNativeMethod("iterator", ((fiber, ip, bp, sp, argCount, target) -> {
                var it = new CMapIterator((CMap)fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0)));
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(it));
                return ip + 1;
            }));

            builder.withNativeMethod("compact", ((fiber, ip, bp, sp, argCount, target) -> {
                ((CMap) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).compact();
                fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // return self
                return ip + 1;
            }));

            builder.withNativeMethod("clear", ((fiber, ip, bp, sp, argCount, target) -> {
                ((CMap) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).clear();
                fiber.pushResult(bp, sp, argCount, fiber.readArg(bp, sp, argCount, 0)); // return self
                return ip + 1;
            }));

            builder.withNativeMethod("size", ((fiber, ip, bp, sp, argCount, target) -> {
                var size = ((CMap) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 0))).size();
                fiber.pushResult(bp, sp, argCount, size);
                return ip + 1;
            }));

        });

        binding.register(CMapIterator.class, builder -> {
            builder.withNativeMethod("hasNext", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, ((CMapIterator) target).hasNext() ? 1 : 0);
                return ip + 1;
            }));

            builder.withNativeMethod("next", ((fiber, ip, bp, sp, argCount, target) -> {
                fiber.pushResult(bp, sp, argCount, ((CMapIterator) target).next());
                return ip + 1;
            }));
        });

        binding.register(String.class, builder -> {
            builder.withNativeMethod("plus", ((fiber, ip, bp, sp, argCount, target) -> {
                var v = fiber.readArg(bp, sp, argCount, 1);
                var other = Value.isNumber(v) ? Double.toString(v) : Objects.toString(fiber.vm().heap().read(v));
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(((String) target).concat(other)));
                return ip + 1;
            }));

            builder.withNativeMethod("equals", ((fiber, ip, bp, sp, argCount, target) -> {
                var s = (String) target;
                var other = fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 1));
                fiber.pushResult(bp, sp, argCount, s.equals(other) ? 1 : 0);
                return ip + 1;
            }));

            builder.withNativeMethod("mod", ((fiber, ip, bp, sp, argCount, target) -> {
                var params = (CList) fiber.vm().toHostValue(fiber.readArg(bp, sp, argCount, 1));
                var formatted = ((String) target).formatted(params.toHostArray());
                fiber.pushResult(bp, sp, argCount, fiber.vm().fromHostValue(formatted));
                return ip + 1;
            }));
        });

        binding.register(CMethodBinding.class, builder -> {
            builder.withNativeMethod("call", ((fiber, ip, bp, sp, argCount, target) -> {
                var methodBinding = (CMethodBinding) target;

                var boundArgs = methodBinding.args();
                var totalArgs = argCount + boundArgs.size();

                // We use a +1 offset so that parameter indices align with the method's formal parameter list, skipping self
                var argIndex = methodBinding.callIndex() + 1;

                // Shift the current args up the stack (skipping self)
                System.arraycopy(fiber.stack, bp + sp - argCount + argIndex, fiber.stack, bp + sp - argCount + argIndex + boundArgs.size(), argCount - 1);

                // Bulk copy the bound args to the stack
                System.arraycopy(boundArgs.rawStorage(), 0, fiber.stack, bp + sp - argCount + argIndex, boundArgs.size());

                // Overwrite self
                fiber.pushResult(bp, sp, argCount, methodBinding.target());
                return methodBinding.dynamicCall(fiber, ip, bp, sp + boundArgs.size(), methodBinding.methodName(), totalArgs);
            }));

            builder.withNativeMethod("bindArgs", ((fiber, ip, bp, sp, argCount, target) -> {
                var methodBinding = (CMethodBinding) target;
                var heap = fiber.vm().heap();
                var argIndex = fiber.readArg(bp, sp, argCount, 1);
                if(!Value.isNumber(argIndex)){
                    throw new TypeError(fiber, "argIndex must be a number");
                }

                var argList = (CList) heap.read(fiber.readArg(bp, sp, argCount, 2));
                methodBinding.bindArgs((int) argIndex, argList);

                return ip + 1;
            }));
        });
    }
}
