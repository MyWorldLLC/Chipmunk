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

package chipmunk.runtime;

import java.lang.invoke.SwitchPoint;
import java.lang.reflect.Field;
import java.util.concurrent.locks.ReentrantLock;

public class TraitField {
    protected final String field;
    protected final ReentrantLock lock;
    protected volatile SwitchPoint invalidationPoint;
    protected volatile Field reflectedField;

    public TraitField(String fieldName){
        field = fieldName;
        lock = new ReentrantLock();
        invalidationPoint = new SwitchPoint();
    }

    public String getField(){
        return field;
    }

    public ReentrantLock getLock(){
        return lock;
    }

    public SwitchPoint getInvalidationPoint(){
        return invalidationPoint;
    }

    public synchronized void invalidateAndReset(){
        SwitchPoint.invalidateAll(new SwitchPoint[]{invalidationPoint});
        invalidationPoint = new SwitchPoint(); // Reset so future method bindings do not get bound to the invalidated switch point
    }

    public Field getReflectedField(){
        return reflectedField;
    }

    public void setReflectedField(Field f){
        reflectedField = f;
    }

    @Override
    public String toString(){
        return "TraitField[" + field + "]";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof TraitField){
            return ((TraitField) o).field.equals(field);
        }
        return false;
    }

    public static boolean isTrait(TraitField[] fields, String fieldName){
        if(fields == null){
            return false;
        }

        for(TraitField f : fields){
            if(f.getField().equals(fieldName)){
                return true;
            }
        }

        return false;
    }

    public static TraitField getField(TraitField[] fields, String fieldName){
        if(fields == null){
            return null;
        }

        for(TraitField f : fields){
            if(f.getField().equals(fieldName)){
                return f;
            }
        }

        return null;
    }
}
