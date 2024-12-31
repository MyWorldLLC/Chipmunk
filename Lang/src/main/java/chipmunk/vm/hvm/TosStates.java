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

package chipmunk.vm.hvm;

import java.util.HashMap;
import java.util.Map;

public class TosStates {

    private final Map<Integer, Integer> phi = new HashMap<>();

    public void markOpcode(int opIndex, int tos){
        if(!phi.containsKey(opIndex)){
            phi.put(opIndex, tos);
        }
    }

    public int getTosRegister(int opIndex){
        var reg = phi.get(opIndex);
        return reg == null ? -1 : reg;
    }

}
