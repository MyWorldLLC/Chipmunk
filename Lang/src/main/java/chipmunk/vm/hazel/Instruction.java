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

package chipmunk.vm.hazel;

public abstract class Instruction {

    protected final int sp;

    protected Instruction(int sp) {
        this.sp = sp;
    }

    public int sp(){
        return sp;
    }

    public abstract int apply(Fiber fiber, int ip, int bp);

    public final void dynamicCall(Fiber fiber, int stackAddr, double ptr, String method, int args){
        // TODO
    }

    public String toString() {
        return getClass().getSimpleName() + " sp=" + sp;
    }
}
