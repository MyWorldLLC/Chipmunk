/*
 * Copyright (C) 2023 MyWorld, LLC
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

package chipmunk.vm.jvm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TrapConfig {

    protected final Set<TrapFlag> enabledTraps;

    public TrapConfig(){
        enabledTraps = new HashSet<>();
    }

    public void enable(TrapFlag flag){
        enabledTraps.add(flag);
    }

    public boolean isEnabled(TrapFlag... flags){
        return Arrays.stream(flags).anyMatch(enabledTraps::contains);
    }

    public void disable(TrapFlag flag){
        enabledTraps.remove(flag);
    }

}
