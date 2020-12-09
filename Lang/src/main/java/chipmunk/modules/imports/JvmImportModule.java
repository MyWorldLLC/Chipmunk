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

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.invoke.security.LinkingPolicy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class JvmImportModule extends ImportModule {

    public Class<?> importJava(String clsName) throws ClassNotFoundException {
        return Class.forName(clsName);
    }

    public Object newJava(String clsName) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return newJava(clsName, null);
    }

    public Object newJava(String clsName, List<Object> args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> cls = importJava(clsName);

        Constructor<?> constructor;
        if(args == null){
            constructor = cls.getConstructor();
        }else{
            Class<?>[] argTypes = new Class<?>[args.size()];
            for(int i = 0; i < args.size(); i++){
                Object arg = args.get(i);
                argTypes[i] = arg == null ? null : arg.getClass();
            }
            constructor = cls.getConstructor(argTypes);
        }

        Object[] constructorArgs = args != null ? args.toArray() : new Object[]{};

        ChipmunkScript script = ChipmunkScript.getCurrentScript();
        if(script != null){
            LinkingPolicy policy = script.getLinkPolicy();
            if(policy != null){
                boolean allowed = policy.allowInstantiation(cls, constructorArgs);
                if(!policy.allowInstantiation(cls, constructorArgs)){
                    throw new IllegalAccessException(
                            String.format("Script %d forbidden from instantiating %s(%s)",
                                    script.getId(),
                                    cls.getName(),
                                    Arrays.asList(constructorArgs)));
                }
            }
        }

        return constructor.newInstance(constructorArgs);
    }

}
