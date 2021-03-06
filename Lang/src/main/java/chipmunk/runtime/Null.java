/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.runtime;

public class Null {

    private static final Null instance = new Null();

    private Null(){}

    public static Null getInstance(){
        return instance;
    }

    public static Object wrapNull(Object o){
        return o == null ? instance : o;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof Null;
    }

    @Override
    public int hashCode(){
        return getClass().getName().hashCode();
    }

    @Override
    public String toString(){
        return "Null";
    }
}
