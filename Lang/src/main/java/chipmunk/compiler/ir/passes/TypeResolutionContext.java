/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.compiler.ir.passes;

import java.util.ArrayDeque;
import java.util.Deque;

public class TypeResolutionContext {

    protected final Deque<Runnable> inferrenceTasks = new ArrayDeque<>();

    public void enqueueTask(Runnable task){
        inferrenceTasks.add(task);
    }

    public void flushTasks(){
        while(!inferrenceTasks.isEmpty()){
            for(Runnable task : inferrenceTasks){
                task.run();
            }
        }
    }
}
