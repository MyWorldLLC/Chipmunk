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

package chipmunk.util.pattern;

import chipmunk.compiler.ast.uniform.AstNode;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.types.ObjectType;

import static chipmunk.compiler.lexer.TokenType.*;

import java.util.List;
import java.util.Optional;

import static chipmunk.compiler.lexer.TokenType.EQUALS;

public class RecognizerExperiment {

    public void register(PatternRecognizer<Token, TokenStream, TokenType, AstNode> recognizer) {
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        recognizer.define(f.when(FINAL, VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(2), true)))
                .define(f.when(VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(2), false)));
    }

    protected AstNode parseVarDec(TokenStream t, Token identifier, boolean isFinal) {
        return new AstNode(
                NodeType.VAR_DEC, ObjectType.ANY, identifier,
                Optional.of(new Symbol(identifier.text(), isFinal)),
                t.peek().type() == EQUALS ? List.of(parseExpression(t)) : List.of());
    }

    protected AstNode parseExpression(TokenStream t) {
        return null;
    }
}
