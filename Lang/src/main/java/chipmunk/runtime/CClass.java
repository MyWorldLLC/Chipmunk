/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.runtime;

import java.util.ArrayList;
import java.util.List;

public class CClass {

    final String name;

    final CField[] sharedFields;
    final CMethod[] sharedMethods;

    final CField[] instanceFields;
    final CMethod[] instanceMethods;
    final int[] traits;

    public CClass(String name, int iMethods, int iFields, int sMethods, int sFields){
        this(name, iMethods, iFields, sMethods, sFields, 0);
    }

    public CClass(String name, int iMethods, int iFields, int sMethods, int sFields, int traits){

        this.name = name;

        instanceMethods = new CMethod[iMethods];
        instanceFields = new CField[iFields];
        this.traits = new int[traits];

        sharedMethods = new CMethod[sMethods];
        sharedFields = new CField[sFields];
    }

    public CMethod getMethod(CObject obj, String name, int args){
        for(int i = 0; i < instanceMethods.length; i++){
            var m = instanceMethods[i];
            if(m.argCount == args && m.name.equals(name)){
                return m;
            }
        }
        if(traits != null){
            for(int t = 0; t < traits.length; t++){
                if(obj.fields[traits[t]] instanceof CObject traitObj){
                    var m = traitObj.cls.getMethod(traitObj, name, args);
                    if(m != null){
                        return m;
                    }
                }
            }
        }

        return null;
    }

    public CMethod getInstanceMethod(int index){
        return instanceMethods[index];
    }

    public CMethod getInstanceMethod(String name){
        for(var method : instanceMethods){
            if(method.name.equals(name)){
                return method;
            }
        }
        return null;
    }

    public void defineInstanceMethod(int i, CMethod method){
        instanceMethods[i] = method;
    }

    public boolean isTrait(int field){
        return instanceFields[field].isTrait();
    }

    public int getFieldIndex(String name){
        for(int i = 0; i < instanceFields.length; i++){
            var f = instanceFields[i];
            if(f.name().equals(name)){
                return i;
            }
        }
        return -1;
    }

    public CObject instantiate(){
        var obj = new CObject();
        obj.cls = this;
        obj.fields = new Object[instanceFields.length];
        return obj;
    }

    public static Builder builder(String name){
        return new Builder(name);
    }

    public static class Builder {

        private final String name;
        private final List<CMethod> methods = new ArrayList<>();
        private final List<CField> fields = new ArrayList<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder withInstanceMethod(CMethod method){
            methods.add(method);
            return this;
        }

        public Builder withInstanceField(CField field){
            fields.add(field);
            return this;
        }

        public CClass build(){
            var cls = new CClass(name, methods.size(), fields.size(), 0, 0);
            for(int i = 0; i < methods.size(); i++){
                cls.instanceMethods[i] = methods.get(i);
            }
            for(int i = 0; i < fields.size(); i++){
                cls.instanceFields[i] = fields.get(i);
            }
            return cls;
        }
    }

}
