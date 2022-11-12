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

package chipmunk.compiler.ast.uniform;

import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.compiler.types.ObjectType;

import java.util.Collection;
import java.util.List;

public record AstNode(NodeType type, ObjectType expressionType, Token origin, SymbolTable symbols, List<AstNode> children) {

    public AstNode(NodeType type, ObjectType expressionType, Token origin, List<AstNode> children){
        this(type, expressionType, origin, null, children);
    }

    public static AstNode create(NodeType type, ObjectType expressionType, Token origin, List<AstNode> children){
        return type.isBlock()
                ? new AstNode(type, expressionType, origin, new SymbolTable(), children)
                : new AstNode(type, expressionType, origin, null, children);
    }

    public boolean is(NodeType type){
        return this.type == type;
    }

    public boolean isBlock(){
        return type.isBlock();
    }

    public void visit(Collection<Visitor> visitors){
        visit(new VisitTrace(), visitors);
    }

    private void visit(VisitTrace trace, Collection<Visitor> visitors){
        trace.enter(this);
        visitors.forEach(v -> v.visit(trace, this));
        children.forEach(c -> c.visit(trace, visitors));
        trace.exit();
    }
}
