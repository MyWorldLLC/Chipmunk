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
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import static chipmunk.compiler.lexer.TokenType.*;

import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.parser.ExpressionParser;
import chipmunk.compiler.parser.Parser;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

public class StatementParser implements Parser<AstNode> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> recognizer;

    public StatementParser(){
        recognizer = new PatternRecognizer<>(Token::type);
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        recognizer.define(f.when(IF).then(t -> new IfElseParser().parse(t)));
        recognizer.define(f.when(IF).then(t -> new IfElseParser().parse(t)));
        recognizer.ignore(NEWLINE, COMMENT);
    }

    @Override
    public AstNode parse(TokenStream t) throws SyntaxError {
        if(t.peek().type().isKeyword()){
            var node = recognizer.matchAll(t);
            if(node == null){
                ChipmunkParser.syntaxError("Unexpected token", t.getFileName(), t.peek(),
                        IF, WHILE, FOR, TRY,
                        RETURN, THROW, BREAK, CONTINUE);
            }
            return node;
        }else if(!t.peek(EOF)){
            return new ExpressionParser(t).parseExpression();
        }
        // This should never happen in practice - either the expression parser or recognizer
        // will match or throw.
        return null;
    }

}
