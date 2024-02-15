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
import chipmunk.compiler.ast.NodeType;
import chipmunk.compiler.ast.VarDec;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ExpressionParser;
import chipmunk.compiler.parser.Parser;
import chipmunk.compiler.types.ObjectType;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import static chipmunk.compiler.lexer.TokenType.*;

public class VarDecParser implements Parser<AstNode> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> declaration;
    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> assignment;

    protected final boolean acceptTraits;
    protected final boolean acceptImplicits;

    public VarDecParser(){
        this(false, false);
    }

    public VarDecParser(boolean acceptTraits){
        this(acceptTraits, false);
    }

    public VarDecParser(boolean acceptTraits, boolean acceptImplicits){
        this.acceptTraits = acceptTraits;
        this.acceptImplicits = acceptImplicits;
        declaration = new PatternRecognizer<>(Token::type);
        assignment = new PatternRecognizer<>(Token::type);
        registerDeclaration();
        registerAssignment();
    }

    protected void registerDeclaration(){
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        declaration.define(f.when(FINAL, VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(1), t.get(), true, false)));
        declaration.define(f.when(VAR, IDENTIFIER).then(t -> parseVarDec(t, t.get(), t.get(), false, false)));
        if(acceptImplicits){
            declaration.define(f.when(FINAL, IDENTIFIER).then(t -> parseVarDec(t, t.get(), t.get(), true, false)));
            declaration.define(f.when(IDENTIFIER).then(t -> {
                var id = t.get();
                return parseVarDec(t, id, id, false, false);
            }));
        }
        if(acceptTraits){
            declaration.define(f.when(FINAL, TRAIT, IDENTIFIER).then(t -> parseVarDec(t, t.get(1), t.get(), true, true)));
            declaration.define(f.when(TRAIT, IDENTIFIER).then(t -> parseVarDec(t, t.get(), t.get(), false, true)));
        }
    }

    protected void registerAssignment(){
        var f = new PatternFactory<Token, TokenStream, TokenType, AstNode>();
        assignment.define(f.when(EQUALS).then(t -> {
            t.dropNext();
            return new ExpressionParser(t).parseExpression();
        }));
    }

    protected AstNode parseVarDec(TokenStream t, Token dec, Token identifier, boolean isFinal, boolean isTrait) {
        var node = VarDec.make(dec, identifier);
        node.getSymbol().setFinal(isFinal);
        node.getSymbol().setTrait(isTrait);

        if(t.peek(COLON)){
            t.dropNext(COLON);
            node.setResultTypeName(t.getNext(IDENTIFIER));
        }

        var expr = assignment.matchAll(t);
        if(expr != null){
            node.addChild(expr);
        }

        return node;
    }

    @Override
    public AstNode parse(TokenStream t){
        return declaration.matchAll(t);
    }

    @Override
    public boolean peek(TokenStream t){
        return declaration.testAll(t);
    }
}
