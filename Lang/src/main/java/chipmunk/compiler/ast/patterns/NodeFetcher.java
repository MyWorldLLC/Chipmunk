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

package chipmunk.compiler.ast.patterns;

import chipmunk.compiler.ast.AstNode;

import java.util.function.Function;

public interface NodeFetcher extends Function<AstNode, AstNode> {

    static NodeFetcher child(int index){
        return p -> p.getChild(index);
    }

    static NodeFetcher child(NodeFetcher delegate, int index){
        return p -> delegate.apply(child(index).apply(p));
    }

    static NodeFetcher left(){
        return AstNode::getLeft;
    }

    static NodeFetcher left(NodeFetcher delegate){
        return p -> delegate.apply(left().apply(p));
    }

    static NodeFetcher right(){
        return AstNode::getRight;
    }

    static NodeFetcher right(NodeFetcher delegate){
        return p -> delegate.apply(right().apply(p));
    }

    static NodeFetcher unary(){
        return AstNode::getChild;
    }

    static NodeFetcher unary(NodeFetcher delegate){
        return p -> delegate.apply(unary().apply(p));
    }

}
