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

package chipmunk.vm.tree;

public class CClass {

    CField[] sharedFields;
    CMethod[] sharedMethods;

    CField[] instanceFields;
    CMethod[] instanceMethods;
    int[] traits;

    public CClass(String name, int iMethods, int iFields, int sMethods, int sFields){
        instanceMethods = new CMethod[iMethods];
        instanceFields = new CField[iFields];
        traits = new int[0]; // TODO

        sharedMethods = new CMethod[sMethods];
        sharedFields = new CField[sFields];
    }

    // TODO - init traits from fields

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

    public void defineMethod(int i, CMethod method){
        instanceMethods[i] = method;
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

}
