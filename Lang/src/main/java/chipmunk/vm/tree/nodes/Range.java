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

import chipmunk.runtime.BigDecimalRange;
import chipmunk.vm.tree.Fiber;
import chipmunk.vm.tree.Node;

import java.math.BigDecimal;

public class Range implements Node {

    private static final BigDecimal STEP = new BigDecimal(1);

    protected final Node start, end;
    protected final boolean inclusive;

    public Range(Node start, Node end, boolean inclusive){
        this.start = start;
        this.end = end;
        this.inclusive = inclusive;
    }

    @Override
    public Object execute(Fiber ctx) {
        // TODO - suspension
        BigDecimal s = (BigDecimal) start.execute(ctx);
        BigDecimal e = (BigDecimal) end.execute(ctx);

        return new BigDecimalRange(s, e, STEP, inclusive);
    }
}
