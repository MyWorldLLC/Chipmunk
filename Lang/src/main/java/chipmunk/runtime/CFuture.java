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

package chipmunk.runtime;

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.hazel.Fiber;
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.invoke.AllowChipmunkLinkage;
import chipmunk.vm.invoke.ChipmunkName;

public class CFuture {

    protected final HazelVM vm;

    protected volatile double value;
    protected volatile boolean isSet;
    protected volatile boolean isFailed;
    protected volatile Fiber waiting;
    protected volatile boolean blockingWait;

    public CFuture(HazelVM vm) {
        this.vm = vm;
    }

    public void success(Object value) {
        this.value = vm.fromHostValue(value);
        isSet = true;
        wakeUp();
    }

    public void failed(String message) {
        value = vm.fromHostValue(message);
        isFailed = true;
        isSet = true;
        wakeUp();
    }

    private void wakeUp(){
        if(waiting != null){
            if(blockingWait){
                waiting.unblock();
            }else{
                waiting.vm().clearSleep(waiting);
            }
            waiting = null;
        }
    }

    public static CFuture failed(HazelVM vm, String message) {
        var f = new CFuture(vm);
        f.failed(message);
        return f;
    }

    /**
     * Wait indefinitely for the future to complete
     */
    @AllowChipmunkLinkage
    @ChipmunkName("wait")
    public void _wait(){
        var script = ChipmunkScript.getCurrentScript();
        var fiber = script.getHazelVM().currentFiber();
        fiber.block();
        waiting = fiber;
        script.yield();
        blockingWait = true;
    }

    /**
     * Wait for up to the passed number of milliseconds, waking
     * immediately when the value is available.
     * @param millis maximum milliseconds to sleep for
     */
    @AllowChipmunkLinkage
    public void waitFor(double millis){
        var script = ChipmunkScript.getCurrentScript();
        waiting = script.getHazelVM().currentFiber();
        script.getHazelVM().sleep((long)millis);
        blockingWait = false;
    }

    @AllowChipmunkLinkage
    public double getValue() {
        return value;
    }

    @AllowChipmunkLinkage
    public boolean isComplete() {
        return isSet;
    }

    @AllowChipmunkLinkage
    public boolean isFailed() {
        return isFailed;
    }

    @AllowChipmunkLinkage
    public boolean isSuccess() {
        return isSet && !isFailed;
    }

}
