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

package chipmunk.compiler.ast;

import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;

public class Operators {

    public static AstNode make(String op, AstNode... operands){
        return make(op, TokenType.typeFor(op), operands);
    }

    public static AstNode make(String op, TokenType type, AstNode... operands){
        return make(op, type, Token.UNKNOWN, operands);
    }

    public static AstNode make(String op, TokenType type, int line, AstNode... operands){
        return new AstNode(NodeType.OPERATOR, new Token(op, type, line), operands);
    }

    public static boolean isDotCall(AstNode node){
        return node.is(NodeType.OPERATOR) && node.token.type() == TokenType.LPAREN
                && node.getLeft().getToken().type() == TokenType.DOT
                && node.getLeft().getRight().is(NodeType.ID);
    }

    public static boolean isRawCall(AstNode node){
        return false; // TODO
    }

    public static boolean isAssignment(AstNode node){
        return node.is(NodeType.OPERATOR)
                && node.getToken().type() == TokenType.EQUALS;
    }

    public static boolean isSetAt(AstNode node){
        return isAssignment(node)
                && node.getLeft().getToken().type() == TokenType.LBRACKET;
    }

    public static boolean isSetAttr(AstNode node){
        return isAssignment(node) && node.getLeft().getToken().type() == TokenType.DOT;
    }

    public static String getSetAttrName(AstNode node){
        return node.getLeft().getRight().getToken().text();
    }

    public static AstNode getSetAttrTarget(AstNode node){
        return node.getLeft().getLeft();
    }

    public static String getDotCallMethodName(AstNode node){
        return node.getLeft().getRight().getToken().text();
    }

    public static AstNode getDotCallTarget(AstNode node){
        return node.getLeft().getLeft();
    }

    public static void visitDotCallParams(AstNode node, AstVisitor visitor){
        node.visitChildren(visitor, 1);
    }
}
