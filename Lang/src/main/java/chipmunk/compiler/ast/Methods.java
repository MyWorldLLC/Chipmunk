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
        node.addChild(new AstNode(NodeType.PARAM_LIST));
        addParam(node, Identifier.make("self", name.line()));
        return node;
    }

    public static Symbol getName(AstNode node){
        ensureMethod(node);
        return node.getSymbol();
    }

    public static boolean isMethodNamed(AstNode node, String name){
        return node.is(NodeType.METHOD) && node.getSymbol().isNamed(name);
    }

    public static boolean isNameOfMethodNode(AstNode node, String name){
        return node.is(NodeType.METHOD) && node.getSymbol().isNamed(name);
    }

    public static void addParam(AstNode node, AstNode param){
        ensureMethod(node);
        node.getChild(0).addChild(param);
    }

    public static int getParamCount(AstNode node){
        ensureMethod(node);
        return node.getChild(0).childCount();
    }

    public static int getBodyNodeCount(AstNode node){
        ensureMethod(node);
        return node.childCount() - 1;
    }

    public static int childIndexToBodyIndex(int index){
        return index - 1;
    }

    public static int bodyIndexToChildIndex(int index){
        return index + 1;
    }

    public static void addToBody(AstNode node, int bodyIndex, AstNode b){
        node.addChild(bodyIndexToChildIndex(bodyIndex), b);
    }

    public static void addToBody(AstNode node, AstNode b){
        node.addChild(b);
    }

    public static void ensureMethod(AstNode node){
        Require.require(node.is(NodeType.METHOD), "%s is not a method", node.getNodeType());
    }

    public static String anonymousName(int line, int column){
        return "anon$L" + line + "C" + column;
    }

    public static boolean isAnonymousName(String name){
        return name.contains("anon$L");
    }

    public static AstNode makeInvocation(AstNode target, String name, int line, AstNode... params){
        var callNode = Operators.make("(", TokenType.LPAREN, line,
                Operators.make(".", TokenType.DOT, line,
                        target,
                        Identifier.make(name))
        );
        callNode.addChildren(params);
        return callNode;
    }

    public static AstNode makeInvocation(AstNode target, String name, AstNode... params){
        return makeInvocation(target, name, Token.UNKNOWN, params);
    }

    public static void visitParams(AstNode node, AstVisitor visitor){
        ensureMethod(node);
        node.getChild(0).visit(visitor);
    }

    public static void visitBody(AstNode node, AstVisitor visitor){
        ensureMethod(node);
        node.visitChildren(visitor, 1);
    }
}
