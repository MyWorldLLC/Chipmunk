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

public class Conversions {

    public static boolean toBoolean(Object o){
        return o != null && (
                ((o instanceof Boolean b) && b)
                || toInt(o) != 0
        );
    }

    public static int toInt(Object o){
        return ((Number)o).intValue();
    }

    public static long toLong(Object o){
        return ((Number)o).longValue();
    }

    public static float toFloat(Object o){
        return ((Number)o).floatValue();
    }

    public static double toDouble(Object o){
        return ((Number)o).doubleValue();
    }
}
