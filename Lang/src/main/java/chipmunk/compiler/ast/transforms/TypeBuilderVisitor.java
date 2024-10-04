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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.types.ObjectType;

public class TypeBuilderVisitor implements AstVisitor {

    @Override
    public void visit(AstNode node) {
        node.visitChildren(this);

        switch (node.getNodeType()){
            case LITERAL -> {
                if(node.getToken().type() == TokenType.INTLITERAL){
                    node.setResultType(ObjectType.INT);
                }else if(node.getToken().type() == TokenType.FLOATLITERAL){
                    node.setResultType(ObjectType.FLOAT);
                }
            }
            case OPERATOR -> {
                if(node.getToken().type() == TokenType.PLUS){
                    node.setResultType(ObjectType.INT); // TODO - match off of children
                }
            }
        }
    }
}
