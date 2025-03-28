/*
 * Copyright (C) 2025 MyWorld, LLC
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

import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CMap {

    // TODO - memory tracing
    // TODO - this needs to be replaced with an implementation that will
    // use CObject.hash() & CObject.equals()
    private final Map<Object, Object> backing;

    public CMap(){
        backing = new HashMap<>();
    }

    public CMap(int initialCapacity){
        backing = new HashMap<>(initialCapacity);
    }

    @AllowChipmunkLinkage
    public Object put(Object key, Object value){
        return setAt(key, value);
    }

    @AllowChipmunkLinkage
    public Object setAt(Object key, Object value){
        return backing.put(key, value);
    }

    @AllowChipmunkLinkage
    public Object get(Object key){
        return getAt(key);
    }

    @AllowChipmunkLinkage
    public Object getAt(Object key){
        return backing.get(key);
    }

    @AllowChipmunkLinkage
    public Object remove(Object key){
        return backing.remove(key);
    }

    @AllowChipmunkLinkage
    public BigDecimal size(){
        return new BigDecimal(backing.size());
    }

    @AllowChipmunkLinkage
    public Boolean contains(Object key){
        return backing.containsKey(key);
    }

    @AllowChipmunkLinkage
    public Boolean containsValue(Object value){
        return backing.containsValue(value);
    }

}
