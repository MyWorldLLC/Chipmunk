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

package chipmunk.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class SymbolStorage<T extends Named> {

    protected final SymbolStorage<T> parent;
    protected int offset;
    protected final List<T> elements;

    public SymbolStorage() {
        this(null);
    }

    /**
     * Parent is used for nested local scopes. It shouldn't be used for classes/modules.
     */
    public SymbolStorage(SymbolStorage<T> parent){
        this.offset = parent != null ? parent.offset + parent.elements.size() : 0;
        this.parent = parent;
        this.elements = new ArrayList<>();
    }

    public boolean has(String name) {
        return elements.stream().anyMatch(v -> v.name().equals(name));
    }

    public int indexOf(String name) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).name().equals(name)) {
                return offset + i;
            }
        }
        throw new NoSuchElementException(name + " not found");
    }

    public SymbolStorage<T> declare(T element) {
        if(has(element.name())){
            throw new IllegalStateException(element.name() + " already exists");
        }
        elements.add(element);
        return this;
    }

    public T get(int index) {
        return elements.get(index - offset);
    }

    public T get(String name){
        return get(indexOf(name));
    }

    public int markOffset(){
        offset = parent != null ? parent.offset + parent.elements.size() : 0;
        return offset;
    }

    public int offset(){
        return offset;
    }

    public int count(){
        return elements.size();
    }

    public List<T> elements(){
        return Collections.unmodifiableList(elements);
    }

    public SymbolStorage<T> parent(){
        return parent;
    }
}
