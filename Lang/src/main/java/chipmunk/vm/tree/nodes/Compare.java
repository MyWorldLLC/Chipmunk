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

public class Compare implements Node {

    public enum Comparison {
        LT,
        LE,
        EQ,
        GE,
        GT
    }

    protected final Node l, r;
    protected final Comparison comparison;

    public Compare(Node l, Node r, Comparison comparison){
        this.l = l;
        this.r = r;
        this.comparison = comparison;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(Fiber ctx) {
        // TODO - support CObject.compare()
        // TODO - suspension
        var lResult = (Comparable<Object>) l.execute(ctx);
        var rResult = r.execute(ctx);

        return switch (comparison){
            case LT -> lResult.compareTo(rResult) < 0;
            case LE -> lResult.compareTo(rResult) <= 0;
            case EQ -> lResult.compareTo(rResult) == 0;
            case GE -> lResult.compareTo(rResult) >= 0;
            case GT -> lResult.compareTo(rResult) > 0;
        };
    }

}
