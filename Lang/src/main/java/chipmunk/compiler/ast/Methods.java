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

public class Methods {

    public static AstNode make(String name){
        return make(new Token("def", TokenType.DEF), new Token(name, TokenType.IDENTIFIER));
    }

    public static AstNode make(Token def, String name){
        return make(def, new Token(name, TokenType.IDENTIFIER));
    }

    public static AstNode make(Token def, Token name){
        var node = new AstNode(NodeType.METHOD, def);
        node.setSymbol(new Symbol(name.text()));
        node.addChild(Identifier.make(name));
        node.addChild(new AstNode(NodeType.PARAM_LIST));
        return node;
    }

    public static AstNode getName(AstNode node){
        return node.getChild(0);
    }

    public static boolean isMethodNamed(AstNode node, String name){
        return node.is(NodeType.METHOD) && node.hasChildren() && Identifier.isIdentifierNamed(node.getChild(0), name);
    }

    public static boolean isNameOfMethodNode(AstNode node, AstNode name){
        return node.is(NodeType.METHOD) && node.hasChildren() && node.getChild(0) == name;
    }

    public static void addParam(AstNode node, AstNode param){
        ensureMethod(node);
        node.getChild(1).addChild(param);
    }

    public static int getParamCount(AstNode node){
        ensureMethod(node);
        return node.getChild(1).childCount();
    }

    public static int getBodyNodeCount(AstNode node){
        ensureMethod(node);
        return node.childCount() - 2;
    }

    public static int childIndexToBodyIndex(int index){
        return index - 2;
    }

    public static int bodyIndexToChildIndex(int index){
        return index + 2;
    }

    public static void addToBody(AstNode node, int bodyIndex, AstNode b){
        node.addChild(bodyIndexToChildIndex(bodyIndex), b);
    }

    public static void addToBody(AstNode node, AstNode b){
        node.addChild(b);
    }

    public static void ensureMethod(AstNode node){
        Require.require(node.is(NodeType.METHOD), "%s is not a method", node.getType());
    }

    public static String anonymousName(int line, int column){
        return "anon$L" + line + "C" + column;
    }

    public static boolean isAnonymousName(String name){
        return name.startsWith("anon$L");
    }

    public static AstNode makeInvocation(AstNode target, String name, AstNode... params){
        var callNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN),
                new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT),
                        target,
                        Identifier.make(new Token(name, TokenType.IDENTIFIER)))
                );
        callNode.addChildren(params);
        return callNode;
    }
}
