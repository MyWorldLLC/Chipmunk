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

import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.util.Require;

public class VarDec {

    public static AstNode makeImplicit(Token varName){
        return make(varName, varName);
    }

    public static AstNode makeImplicit(String varName){
        return makeImplicit(new Token(varName, TokenType.IDENTIFIER));
    }

    public static AstNode make(Token varDec, Token varName){
        var node = new AstNode(NodeType.VAR_DEC, varDec);
        node.addChild(Identifier.make(varName));
        node.setSymbol(new Symbol(varName.text()));
        return node;
    }

    public static AstNode getAssignment(AstNode node){
        Require.require(node.is(NodeType.VAR_DEC), "%s is not a variable declaration", node.getType());
        if(node.childCount() > 1){
            return node.getChild(1);
        }
        return null;
    }

    public static AstNode getIdentifier(AstNode node){
        Require.require(node.is(NodeType.VAR_DEC), "%s is not a variable declaration", node.getType());
        return node.getChild(0);
    }

    public static void removeAssignment(AstNode node){
        if(getAssignment(node) != null){
            node.removeChild(1);
        }
    }

    public static void setAssignment(AstNode node, AstNode expr){
        removeAssignment(node);
        node.addChild(1, expr);
    }

    public static String getVarName(AstNode node){
        Require.require(node.is(NodeType.VAR_DEC), "%s is not a variable declaration", node.getType());
        return getIdentifier(node).getToken().text();
    }

}
