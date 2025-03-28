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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO - memory tracing
 */
public class CList implements Iterable<Object> {

    private final List<Object> backing;

    public CList(){
        backing = new ArrayList<>();
    }

    public CList(int initialCapacity){
        backing = new ArrayList<>(initialCapacity);
    }

    @AllowChipmunkLinkage
    public Boolean add(Object e){
        return backing.add(e);
    }

    @AllowChipmunkLinkage
    public void add(BigDecimal i, Object e){
        backing.add(i.intValueExact(), e);
    }

    @AllowChipmunkLinkage
    public Object set(BigDecimal i, Object e){
        return setAt(i, e);
    }

    @AllowChipmunkLinkage
    public Object get(BigDecimal i){
        return getAt(i);
    }

    @AllowChipmunkLinkage
    public Object setAt(BigDecimal i, Object e){
        return backing.set(i.intValueExact(), e);
    }

    @AllowChipmunkLinkage
    public Object getAt(BigDecimal i){
        return backing.get(i.intValueExact());
    }

    @AllowChipmunkLinkage
    public BigDecimal size(){
        return new BigDecimal(backing.size());
    }

    @AllowChipmunkLinkage
    public Object removeAt(BigDecimal i){
        return backing.remove(i.intValueExact());
    }

    @AllowChipmunkLinkage
    public Boolean remove(Object e){
        return backing.remove(e);
    }

    @AllowChipmunkLinkage
    public Boolean contains(Object o){
        return backing.contains(o);
    }

    @AllowChipmunkLinkage
    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {

            private Iterator<Object> it = backing.iterator();

            @AllowChipmunkLinkage
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @AllowChipmunkLinkage
            @Override
            public Object next() {
                return it.next();
            }
        };
    }
}
