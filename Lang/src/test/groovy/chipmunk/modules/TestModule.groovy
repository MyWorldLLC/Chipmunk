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

package chipmunk.modules

import chipmunk.runtime.ChipmunkModule
import chipmunk.vm.jvm.Uncatchable

class TestModule implements ChipmunkModule {

    static final String TEST_MODULE_NAME = "chipmunk.test"

    def throwUncatchable(){
        throw new Uncatchable()
    }

    void println(Object o){
        System.out.println(o);
    }

    String getName() {
        return TEST_MODULE_NAME
    }
}
