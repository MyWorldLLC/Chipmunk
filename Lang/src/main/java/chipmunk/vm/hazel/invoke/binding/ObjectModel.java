/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm.hazel.invoke.binding;

import chipmunk.vm.hazel.invoke.NativeMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ObjectModel<O> {

    protected final Map<String, FieldModel<O, ?>> fields;
    protected final Map<String, MethodModel<O>> methods;
    protected final Map<String, NativeMethod> nativeMethods;

    public ObjectModel(Map<String, FieldModel<O, ?>> fields, Map<String, MethodModel<O>> methods, Map<String, NativeMethod> nativeMethods) {
        this.fields = fields;
        this.methods = methods;
        this.nativeMethods = nativeMethods;
    }

    @SuppressWarnings("unchecked")
    public <F> FieldModel<O, F> getField(String name){
        return (FieldModel<O, F>) fields.get(name);
    }

    public MethodModel<O> getMethod(String name){
        return methods.get(name);
    }

    public NativeMethod getNativeMethod(String name){
        return nativeMethods.get(name);
    }

    public static class Builder<O> {

        protected final Map<String, FieldModel<O, ?>> fields = new HashMap<>();
        protected final Map<String, MethodModel<O>> methods = new HashMap<>();
        protected final Map<String, NativeMethod> nativeMethods = new HashMap<>();

        public <F> Builder<O> withField(String field, Class<F> type, Function<O, F> getter){
            fields.put(field, new FieldModel<>(type, getter, null));
            return this;
        }

        public <F> Builder<O> withField(String field, Class<F> type, Function<O, F> getter, BiConsumer<O, F> setter){
            fields.put(field, new FieldModel<>(type, getter, setter));
            return this;
        }

        public Builder<O> withMethod(String method, Class<?>[] params, BiFunction<O, Object[], ?> invoker){
            methods.put(method, new MethodModel<>(params, false, invoker));
            return this;
        }

        public Builder<O> withVoidMethod(String method, Class<?>[] params, BiConsumer<O, Object[]> invoker){
            methods.put(method, new MethodModel<>(params, true, (o, p) -> {
                invoker.accept(o, p);
                return null;
            }));
            return this;
        }

        public Builder<O> withNativeMethod(String method, NativeMethod nativeMethod){
            nativeMethods.put(method, nativeMethod);
            return this;
        }

        public ObjectModel<O> build(){
            return new ObjectModel<>(fields, methods, nativeMethods);
        }
    }

    public static <O> Builder<O> builder(){
        return new Builder<>();
    }

}
