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

package chipmunk.vm.jvm;

import chipmunk.vm.invoke.security.LinkingPolicy;

public class JvmCompilerConfig {

    protected final LinkingPolicy linkingPolicy;
    protected final TrapConfig trapConfig;

    public JvmCompilerConfig(LinkingPolicy policy, TrapConfig trapConfig){

        linkingPolicy = policy;
        this.trapConfig = trapConfig;
    }

    public LinkingPolicy getLinkingPolicy(){
        return linkingPolicy;
    }

    public TrapConfig getTrapConfig(){
        return trapConfig;
    }


}
