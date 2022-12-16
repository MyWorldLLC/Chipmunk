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
import chipmunk.compiler.ast.BlockNode;
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.Parser;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import java.util.ArrayList;
import java.util.List;

import static chipmunk.compiler.lexer.TokenType.*;

public class BlockBodyParser implements Parser<List<AstNode>> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, List<AstNode>> recognizer;

    public BlockBodyParser(){
        recognizer = new PatternRecognizer<>(Token::type);
        registerPatterns();
    }

    protected void registerPatterns(){
        var f = new PatternFactory<Token, TokenStream, TokenType, List<AstNode>>();
        recognizer.define(f.when(LBRACE).then(t -> {
            var nodes = parseBlockBody(t);
            t.forceNext(RBRACE);
            return nodes;
        }));
        recognizer.ignore(NEWLINE, COMMENT);
    }

    protected List<AstNode> parseBlockBody(TokenStream t){
        var nodes = new ArrayList<AstNode>();
        var statements = new StatementParser();
        while(!t.peek(RBRACE)){
            nodes.add(statements.parse(t));
        }
        return nodes;
    }

    @Override
    public List<AstNode> parse(TokenStream t) throws SyntaxError {
        return recognizer.matchAll(t);
    }

}
