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

package chipmunk.vm.tree.nodes;

import chipmunk.vm.tree.Fiber;
import chipmunk.vm.tree.Node;

import java.math.BigDecimal;

public class Const implements Node {
    Object value;

    public Const(Object obj){
        value = obj;
    }

    @Override
    public Object execute(Fiber ctx) {
        return value;
    }

    public static Const number(Integer number){
        return new Const(new BigDecimal(number));
    }

    public static Const number(Float number){
        return new Const(new BigDecimal(number));
    }

}
