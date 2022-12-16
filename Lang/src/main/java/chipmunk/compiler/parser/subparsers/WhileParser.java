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

package chipmunk.compiler.parser.subparsers;

import chipmunk.compiler.SyntaxError;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.WhileNode;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.Parser;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import static chipmunk.compiler.lexer.TokenType.*;

public class WhileParser implements Parser<WhileNode> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, WhileNode> recognizer;

    public WhileParser(){
        recognizer = new PatternRecognizer<>(Token::type);

        var f = new PatternFactory<Token, TokenStream, TokenType, WhileNode>();
        recognizer.define(f.when(WHILE, LPAREN).then(this::parseWhile));
    }

    protected WhileNode parseWhile(TokenStream t){
        var token = t.get();
        t.skip(1);

        // TODO - WIP
        //var node = new WhileNode(token);


        return null;
    }

    @Override
    public WhileNode parse(TokenStream t) throws SyntaxError {
        return recognizer.matchAll(t);
    }

}
