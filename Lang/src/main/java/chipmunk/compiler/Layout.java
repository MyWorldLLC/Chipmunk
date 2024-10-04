/*
 * Copyright (C) 2024 MyWorld, LLC
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

import chipmunk.compiler.types.ObjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Layout {

    public static final int WORD_SIZE_STANDARD = 1;
    public static final int WORD_SIZE_DOUBLE = 2;

    public record Field(String name, ObjectType type, int size){}

    protected final String name;
    protected final ObjectType type;
    protected final List<Field> fields;

    public Layout(String name, ObjectType type){
        this.name = name;
        this.type = type;
        fields = new ArrayList<>();
    }

    public boolean hasField(String name){
        return fields.stream()
                .anyMatch(f -> f.name().equals(name));
    }

    public Layout addField(Field field){
        if(hasField(field.name())){
            throw new IllegalArgumentException("Field " + field.name() + " already exists for layout " + name);
        }
        fields.add(field);
        return this;
    }

    public List<Field> getFields(){
        return fields;
    }

    public Optional<Field> getField(String name){
        return fields.stream()
                .filter(field -> field.name().equals(name))
                .findFirst();
    }

}
