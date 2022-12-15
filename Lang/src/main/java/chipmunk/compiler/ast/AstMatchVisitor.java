/*
 * Copyright (C) 2022 MyWorld, LLC
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

package chipmunk.compiler.ast;

import chipmunk.util.Visitor;

public class AstMatchVisitor implements Visitor<AstNode> {

    protected final AstNode node;
    protected int index;

    public AstMatchVisitor(AstNode node){
        this.node = node;
    }

    @Override
    public AstNode get() {
        if(hasMore()){
            var next = node.getChildren().get(index);
            index++;
            return next;
        }
        return null;
    }

    @Override
    public boolean hasMore(){
        return index < node.getChildren().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends Visitor<AstNode>> U duplicate() {
        var sequence = new AstMatchVisitor(node);
        sequence.index = index;
        return (U) sequence;
    }

}
