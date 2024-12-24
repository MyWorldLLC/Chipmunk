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

import java.util.ArrayList;
import java.util.List;

public class IpRemapping {

    private final List<IpRange> remappings = new ArrayList<>();

    public int remap(int ip, int nextIp, int hIp){
        remappings.add(new IpRange(ip, nextIp, hIp));
        return nextIp;
    }

    public IpRange find(int ip){
        for(var range : remappings){
            if(ip >= range.b0() && ip < range.b1()){
                return range;
            }
        }
        return null;
    }

}
