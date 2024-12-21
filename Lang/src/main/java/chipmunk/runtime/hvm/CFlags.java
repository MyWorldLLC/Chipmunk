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

package chipmunk.runtime.hvm;

public class CFlags {

    public static final int SHARED = 0b001;
    public static final int FINAL  = 0b010;
    public static final int TRAIT  = 0b100;

    public static int combine(int... flags){
        var combined = 0;
        for(var f : flags){
            combined |= f;
        }
        return combined;
    }

    public static boolean isSet(int flags, int flag){
        return (flags & flag) != 0;
    }
}
