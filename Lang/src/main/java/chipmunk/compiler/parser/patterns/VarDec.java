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

package chipmunk.compiler.parser.patterns;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ExpressionParser;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import static chipmunk.compiler.lexer.TokenType.*;

public class VarDec {

    public static void register(PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> recognizer) {
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        recognizer.define(f.when(FINAL, VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(2), true)))
                .define(f.when(VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(2), false)));
    }

    public static AstNode parseVarDec(TokenStream t, Token identifier, boolean isFinal) {
        var node = new AstNode(NodeType.VAR_DEC, identifier);
        node.setSymbol(new Symbol(identifier.text(), isFinal));
        node.getChildren().add(parseExpression(t));
        return node;
    }

    protected static AstNode parseExpression(TokenStream t) {
        return new ExpressionParser(t).parseExpression();
    }
}
