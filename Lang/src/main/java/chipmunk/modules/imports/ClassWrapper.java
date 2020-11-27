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

package chipmunk.modules.imports;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ClassWrapper {

    protected final Class<?> wrapped;

    public ClassWrapper(Class<?> cls){
        wrapped = cls;
    }

    public Class<?> getWrappedClass(){
        return wrapped;
    }

    public String getName(){
        return wrapped.getName();
    }

    public Object instantiate(List<Object> args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // TODO - security check
        Constructor<?> constructor;
        if(args == null){
            constructor = wrapped.getConstructor();
        }else{
            Class<?>[] argTypes = new Class<?>[args.size()];
            for(int i = 0; i < args.size(); i++){
                Object arg = args.get(i);
                argTypes[i] = arg == null ? null : arg.getClass();
            }
            constructor = wrapped.getConstructor(argTypes);
        }

        return constructor.newInstance(args.toArray());
    }
}
