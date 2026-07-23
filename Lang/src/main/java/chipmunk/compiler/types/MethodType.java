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

package chipmunk.compiler.types;

import java.util.Arrays;
import java.util.List;

public class MethodType extends ObjectType {

    private final ObjectType rType;
    private final List<ObjectType> pTypes;

    public MethodType(ObjectType rType, List<ObjectType> pTypes) {
        super("Method");
        this.rType = rType;
        this.pTypes = List.copyOf(pTypes);
    }

    public MethodType(ObjectType rType, ObjectType... pTypes){
        this(rType, Arrays.asList(pTypes));
    }

    public ObjectType rType() {
        return rType;
    }

    public List<ObjectType> pTypes() {
        return pTypes;
    }

    @Override
    public boolean isAssignableTo(ObjectType other){
        if(other instanceof MethodType mt){
            if(rType.isAssignableTo(mt.rType) && pTypes.size() == mt.pTypes.size()){
                for(int i = 0; i < pTypes.size(); ++i){
                    if(!pTypes.get(i).isAssignableTo(mt.pTypes.get(i))){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean argsMatch(ObjectType... args){
        return isAssignableTo(new MethodType(AnyType.INSTANCE, args));
    }
}
