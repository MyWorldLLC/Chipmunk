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

package chipmunk.vm.tree.nodes;

public class DebugPrinter {

    protected final StringBuilder builder = new StringBuilder();
    private String indent = "";

    public DebugPrinter enterNode(String name){
        builder.append("(");
        builder.append(name);
        builder.append("\n");
        indent = indent + "  ";
        builder.append(indent);
        return this;
    }

    public DebugPrinter exitNode(){
        builder.append(indent);
        builder.append(")\n");
        indent = indent.substring(0, indent.length() - 2);
        return this;
    }

    public String toString(){
        return builder.toString();
    }

}
