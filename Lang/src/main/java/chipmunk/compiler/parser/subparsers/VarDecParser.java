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

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ExpressionParser;
import chipmunk.compiler.parser.Parser;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import static chipmunk.compiler.lexer.TokenType.*;

public class VarDecParser implements Parser<VarDecNode> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, VarDecNode> declaration;
    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> assignment;

    public VarDecParser(){
        declaration = new PatternRecognizer<>(Token::type);
        assignment = new PatternRecognizer<>(Token::type);
        registerDeclaration();
        registerAssignment();
    }

    protected void registerDeclaration(){
        var f = new PatternFactory<Token, TokenStream, TokenType, VarDecNode>();
        declaration.define(f.when(FINAL, VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(1), t.get(), true)));
        declaration.define(f.when(VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(), t.get(), false)));
    }

    protected void registerAssignment(){
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        assignment.define(f.when(EQUALS).then(t -> {
            t.dropNext();
            return new ExpressionParser(t).parseExpression();
        }));
    }

    protected VarDecNode parseVarDec(TokenStream t, Token dec, Token identifier, boolean isFinal) {
        var node = new VarDecNode(dec);
        node.setVar(new IdNode(identifier));
        node.getSymbol().setFinal(isFinal);

        var expr = assignment.matchAll(t);
        if(expr != null){
            node.setAssignExpr(expr);
        }

        return node;
    }

    @Override
    public VarDecNode parse(TokenStream t){
        return declaration.matchAll(t);
    }
}
