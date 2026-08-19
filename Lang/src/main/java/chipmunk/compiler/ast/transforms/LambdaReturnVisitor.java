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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.Methods;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.codegen.ExpressionVisitor;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;

/**
 * Visits any lambda methods, rewriting them from def(...) <exp> to def(...) return exp.
 * This simplifies the code generator vs having to differentiate between block-bodied & lambda methods.
 */
public class LambdaReturnVisitor implements AstVisitor {
    @Override
    public void visit(AstNode node) {
        node.visitChildren(this);
        if(node.is(NodeType.METHOD)){
            if(Methods.getBodyNodeCount(node) == 1
                    && ExpressionVisitor.isExpressionNode(node.getChild(node.childCount() - 1))){
                var exp = node.getChild(node.childCount() - 1);
                node.removeChild(exp);

                var rNode = new AstNode(NodeType.FLOW_CONTROL, new Token("return", TokenType.RETURN), exp);
                node.addChild(rNode);
            }
        }
    }
}
