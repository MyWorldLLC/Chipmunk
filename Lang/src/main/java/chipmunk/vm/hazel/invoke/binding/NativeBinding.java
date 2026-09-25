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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NativeBinding {

    protected final Map<Class<?>, ObjectModel<?>> models;

    public NativeBinding(){
        models = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <O> ObjectModel<O> modelFor(Class<O> cls){
        // TODO - interfaces?
        var type = (Class<?>) cls;
        while(type != null){
            var model = models.get(cls);
            if(model != null){
                return (ObjectModel<O>) model;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    public <O> NativeBinding register(Class<O> cls, ObjectModel<O> model){
        models.put(cls, model);
        return this;
    }

    public <O> NativeBinding register(Class<O> cls, Consumer<ObjectModel.Builder<O>> builderFn){
        var builder = ObjectModel.<O>builder();
        builderFn.accept(builder);
        models.put(cls, builder.build());
        return this;
    }

}
