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

package chipmunk.compiler.parser.parselets;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ExpressionParser;

public class IfElseParselet implements PrefixParselet {

    @Override
    public AstNode parse(ExpressionParser parser, Token token) {
        TokenStream tokens = parser.getTokens();
        AstNode node = new AstNode(NodeType.OPERATOR, token);
        tokens.skipNewlinesAndComments();
        tokens.dropNext(TokenType.LPAREN);
        node.addChild(parser.parseExpression());
        tokens.skipNewlinesAndComments();
        tokens.dropNext(TokenType.RPAREN);
        tokens.skipNewlinesAndComments();
        node.addChild(parser.parseExpression());
        tokens.skipNewlinesAndComments();
        tokens.dropNext(TokenType.ELSE);
        node.addChild(parser.parseExpression());

        return node;
    }
}
