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

package chipmunk.vm.scheduler;

import chipmunk.vm.ChipmunkVM;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class ScriptRunner extends ForkJoinWorkerThread {

    protected final ChipmunkVM vm;

    /**
     * Creates a ForkJoinWorkerThread operating in the given pool.
     *
     * @param vm the Chipmunk VM this thread works in
     * @param pool the pool this thread works in
     * @throws NullPointerException if pool is null
     */
    public ScriptRunner(ChipmunkVM vm, ForkJoinPool pool) {
        super(pool);
        this.vm = vm;
    }
}
