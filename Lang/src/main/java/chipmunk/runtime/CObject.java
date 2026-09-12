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

public final class CObject extends HostCObject {

    private final double[] storage;
    private final TraitGuard[] guards;

    public CObject(double[] storage) {
        this(storage, null);
    }

    public CObject(double[] storage, TraitGuard[] guards) {
        this.storage = storage;
        this.guards = guards;
    }

    public double classPtr(){
        return storage[0];
    }

    public double getField(int fieldIndex){
        return storage[fieldIndex];
    }

    public double[] storage(){
        return storage;
    }

    public void setField(int fieldIndex, double value){
        storage[fieldIndex] = value;
        if(hasTraits()){
            var guard = getGuard(fieldIndex);
            if(guard != null){
                guard.trip();
                replaceGuard(fieldIndex);
            }
        }
    }

    public TraitGuard getGuard(int fieldIndex){
        for(TraitGuard guard : guards){
            if(guard.guardedField() == fieldIndex)
                return guard;
        }
        return null;
    }

    public boolean hasTraits(){
        return guards != null;
    }

    private void replaceGuard(int fieldIndex){
        if(guards == null) return;
        for(int i = 0; i < guards.length; i++){
            var guard = guards[i];
            if(guard.guardedField() == fieldIndex){
                guards[i] = new TraitGuard(fieldIndex);
            }
        }
    }

}
