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
import chipmunk.compiler.types.Operation;

import java.lang.classfile.CodeBuilder;
import java.util.function.Consumer;

public record OpEmitter(Operation op, Consumer<CodeBuilder> emitter) {

    public static OpEmitter unary(ObjectType pType, Consumer<CodeBuilder> emitter){
        return new OpEmitter(new Operation(pType, pType), emitter);
    }

    public static OpEmitter binOp(ObjectType pType, Consumer<CodeBuilder> emitter){
        return new OpEmitter(new Operation(pType, pType, pType), emitter);
    }

    public static OpEmitter binOp(ObjectType rType, ObjectType pType, Consumer<CodeBuilder> emitter){
        return new OpEmitter(new Operation(rType, pType, pType), emitter);
    }

    public static OpEmitter binOp(ObjectType rType, ObjectType p0, ObjectType p1, Consumer<CodeBuilder> emitter){
        return new OpEmitter(new Operation(rType, p0, p1), emitter);
    }

    public static OpEmitter tertiary(ObjectType rType, ObjectType p0, ObjectType p1, ObjectType p2, Consumer<CodeBuilder> emitter){
        return new OpEmitter(new Operation(rType, p0, p1, p2), emitter);
    }
}
