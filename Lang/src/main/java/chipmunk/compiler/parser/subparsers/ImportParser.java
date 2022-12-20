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
import chipmunk.compiler.ast.Imports;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.compiler.parser.Parser;
import chipmunk.util.pattern.PatternRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static chipmunk.compiler.lexer.TokenType.*;

public class ImportParser implements Parser<AstNode> {

    protected final PatternRecognizer<Token, TokenStream, TokenType, TokenType, AstNode> recognizer;

    public ImportParser(){
        recognizer = new PatternRecognizer<>(Token::type);
        var f = recognizer.factory();
        recognizer.define(f.when(FROM).then(this::parseFromImport));
        recognizer.define(f.when(IMPORT).then(this::parsePlainImport));
    }


    protected AstNode parseFromImport(TokenStream tokens){
        var fromToken = tokens.getNext(FROM);

        var fragments = consumeFragments(tokens, DOT);

        var importToken = tokens.getNext(IMPORT);

        var symbols = consumeFragments(tokens, COMMA);

        if(tokens.peek(AS)){
            var as = tokens.get();
            var aliases = consumeFragments(tokens, COMMA);
            return Imports.makeAliased(
                    fromToken, getModule(fragments, false),
                    importToken, symbols,
                    as, aliases);
        }else{
            return Imports.makePlain(
                    fromToken, getModule(fragments, false),
                    importToken, symbols);
        }
    }

    protected AstNode parsePlainImport(TokenStream tokens){

        var importToken = tokens.getNext(IMPORT);

        var fragments = consumeFragments(tokens, DOT);

        var symbol = getTrailing(fragments);

        if(tokens.peek(AS)){
            var as = tokens.get();
            // Note: multiple aliases is actually illegal here, but our
            // options are to fail at parse time for something that's grammatically
            // plausible, or enforce along with all the other rules about import aliases
            // in the validation layer.
            var aliases = consumeFragments(tokens, COMMA);

            return Imports.makeAliased(
                    importToken, getModule(fragments, fragments.size() > 1),
                    importToken, fragments.size() > 1 ? List.of(symbol) : List.of(),
                    as, aliases);
        }else{
            return Imports.makePlain(
                    importToken, getModule(fragments, fragments.size() > 1),
                    importToken, fragments.size() > 1 ? List.of(symbol) : List.of());
        }
    }

    protected List<Token> consumeFragments(TokenStream tokens, TokenType delimiter){
        var fragments = new ArrayList<Token>();
        while(tokens.peek(IDENTIFIER) || tokens.peek(STAR)){
            fragments.add(tokens.get());
            if(!tokens.peek(delimiter)) {
                break;
            }
            tokens.dropNext();
        }
        return fragments;
    }

    protected Token getModule(List<Token> fragments, boolean ignoreTrailing){
        var module = fragments.subList(0, ignoreTrailing ? fragments.size() - 1 : fragments.size());
        return new Token(module.stream().map(Token::text).collect(Collectors.joining(".")), IDENTIFIER);
    }

    protected Token getTrailing(List<Token> fragments){
        return fragments.get(fragments.size() - 1);
    }

    @Override
    public AstNode parse(TokenStream t) throws SyntaxError {
        var node = recognizer.matchAll(t);
        if(node == null){
            ChipmunkParser.syntaxError("import", t.getFileName(), t.peek(), FROM, IMPORT);
        }
        return node;
    }
}
