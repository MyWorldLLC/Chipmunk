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

import chipmunk.vm.hazel.HazelVM;

public class CClass extends NamedHostObject {

    protected double selfPtr;
    protected double[] sharedFields;
    protected CField[] sharedFieldDefs;
    protected CMethod[] sharedMethodDefs;

    protected CField[] instanceFieldDefs;
    protected CMethod[] instanceMethodDefs;

    public CClass(String name) {
        super(name);
    }

    public double[] sharedFields() {
        return sharedFields;
    }

    public void sharedFields(double[] sharedFields) {
        this.sharedFields = sharedFields;
    }

    public CField[] sharedFieldDefs() {
        return sharedFieldDefs;
    }

    public void sharedFieldDefs(CField[] sharedFieldDefs) {
        this.sharedFieldDefs = sharedFieldDefs;
    }

    public CField[] instanceFieldDefs() {
        return instanceFieldDefs;
    }

    public void instanceFieldDefs(CField[] instanceFieldDefs) {
        this.instanceFieldDefs = instanceFieldDefs;
    }

    public void sharedMethodDefs(CMethod[] sharedMethodDefs) {
        this.sharedMethodDefs = sharedMethodDefs;
    }

    public CMethod[] sharedMethodDefs() {
        return sharedMethodDefs;
    }

    public void instanceMethodDefs(CMethod[] instanceMethodDefs) {
        this.instanceMethodDefs = instanceMethodDefs;
    }

    public CMethod[] instanceMethodDefs() {
        return instanceMethodDefs;
    }

    public double[] createInstanceStorage(HazelVM vm){
        vm.memoryStats().instanceCreated(instanceFieldDefs.length);
        var storage = new double[instanceFieldDefs.length];
        storage[0] = selfPtr;
        return storage;
    }
}
