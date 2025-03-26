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

public class If implements Node {
    public Node test;
    public Node _if;
    public Node _else;

    @Override
    public Object execute(Fiber ctx) {
        boolean t;
        try {
            t = (Boolean) test.execute(ctx);
        } catch (Exception e) {
            throw e;
        }
        if (t) {
            try {
                return _if.execute(ctx);
            } catch (Exception e) {
                throw e;
            }
        } else if (_else != null) {
            return _else.execute(ctx);
        }
        return new BigDecimal(0);
    }
}
