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

package chipmunk.vm.tree;

import static chipmunk.vm.tree.Conversions.*;

public interface Node {
    Object execute(Fiber ctx);

    default boolean executeBoolean(Fiber ctx){
        return toBoolean(execute(ctx));
    }

    default int executeInt(Fiber ctx){
        return toInt(execute(ctx));
    }

    default float executeFloat(Fiber ctx){
        return toFloat(execute(ctx));
    }

    default long executeLong(Fiber ctx){
        return toLong(execute(ctx));
    }

    default double executeDouble(Fiber ctx){
        return toDouble(execute(ctx));
    }
}
