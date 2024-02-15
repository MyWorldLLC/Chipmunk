/*
 * Copyright (C) 2023 MyWorld, LLC
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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.UnresolvedSymbolException;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

public class UpvalueMarkerVisitor implements AstVisitor {

    protected SymbolTable scope;

    @Override
    public void visit(AstNode node) {

        if(node.is(NodeType.IMPORT)){
            return;
        }

        if(node.getNodeType().isBlock()){
            scope = node.getSymbolTable();
        }

        // Recurse to find all non-qualified terminal symbols & rewrite all symbol accesses
        // that are non-local

        // If visiting a variable declaration, don't rewrite the variable name being declared!
        int startIndex = node.is(NodeType.VAR_DEC) ? 1 : 0;
        for(int i = startIndex; i < node.childCount(); i++) {
            AstNode child = node.getChild(i);

            if (child.is(NodeType.ID) && !isQualified(node, child)) {

                if (!isMethodBindTarget(node, i)) {

                    markUpvalue(child);

                }
            } else {
                child.visit(this);
            }
        }

        if(node.getNodeType().isBlock()){
            scope = scope.getParent();
        }

    }

    protected boolean isQualified(AstNode parent, AstNode child){
        if(parent.is(NodeType.OPERATOR)){
            return parent.getToken().text().equals(".") && parent.getRight() == child;
        }
        return false;
    }

    protected void markUpvalue(AstNode child) {

        String symbolName = child.getToken().text();
        Symbol symbol = scope.getSymbol(symbolName);


        if (symbol == null) {
            throw new UnresolvedSymbolException(scope.getModuleScope().getDebugSymbol(), symbolName);
        }

        if (symbol.getTable().isMethodScope()) {
            // Mark local variables that are in an outer method scope as upvalues,
            // and mark the upvalue reference in the nearest method-scope symbol table
            if(!symbol.getName().equals("self") && scope.isOuterLocal(symbol)){
                symbol.markAsUpvalue();
                scope.nearest(SymbolTable.Scope.METHOD).setSymbol(symbol.makeUpvalueRef());
            }
        }
    }

    protected boolean isMethodBindTarget(AstNode node, int index){
        if(index == 1 && node.is(NodeType.OPERATOR)){
            if(node.getToken().type().equals(TokenType.DOUBLECOLON)){
                return true;
            }
        }
        return false;
    }

}
