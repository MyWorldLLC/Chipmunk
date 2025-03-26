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

public class Iflt implements Node {
    public Node l, r;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object execute(Fiber ctx) {
        Object a;
        try {
            a = l.execute(ctx);
        } catch (Exception e) {
            throw e;
        }

        Object b;
        try {
            b = r.execute(ctx);
        } catch (Exception e) {
            throw e;
        }
        return ((Comparable)a).compareTo(b) < 0;
    }
}
