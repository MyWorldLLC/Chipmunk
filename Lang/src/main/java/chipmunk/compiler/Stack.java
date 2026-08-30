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

package chipmunk.compiler;

import chipmunk.compiler.types.ObjectType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Stack {

    protected final Deque<ObjectType> stack  = new ArrayDeque<>();

    public Stack push(ObjectType type) {
        stack.push(type);
        return this;
    }

    public Stack dup(){
        stack.push(stack.peek());
        return this;
    }

    public ObjectType pop(){
        return stack.pop();
    }

    public void pop(int count){
        for(int i = 0; i < count; i++){
            stack.pop();
        }
    }

    public int depth(){
        return stack.size();
    }

    public ObjectType doOperation(Supplier<ObjectType> rType, ObjectType... pTypes){
        // Operands are pushed in forward order, so to check types we have to
        // go in reverse order
        for(int i = pTypes.length - 1; i >= 0; i--){
            var pType = pTypes[i];
            var stackType = stack.pop();
            if(!stackType.isAssignableTo(pType)){
                throw new IllegalStateException("Operand type " + stackType + " is not assignable to " + pType + " @ " + i);
            }
        }
        var resultType = rType.get();
        stack.push(resultType);
        return resultType;
    }

    public ObjectType doOperation(ObjectType rType, ObjectType... pTypes){
        return doOperation(() -> rType, pTypes);
    }

    public Stream<ObjectType> stream(){
        return stack.stream();
    }

}
