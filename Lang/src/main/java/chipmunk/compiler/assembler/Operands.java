/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.compiler.assembler;

import java.util.Stack;

public class Operands {

    protected Stack<HVMType> types = new Stack<>();
    protected final int reserved;

    public Operands(){
        this(0);
    }

    public Operands(int reserved){
        this.reserved = reserved;
    }

    public Operand push(HVMType type){
        types.push(type);
        return new Operand(currentTos(), type);
    }

    public Operand pop(){
        return new Operand(currentTos(), types.pop());
    }

    public Operand dup(){
        var type = types.peek();
        types.push(type);
        return new Operand(currentTos(), type);
    }

    public int currentTos(){
        return reserved + types.size() - 1;
    }

    public int nextPushedTos(){
        return currentTos() + 1;
    }

}
