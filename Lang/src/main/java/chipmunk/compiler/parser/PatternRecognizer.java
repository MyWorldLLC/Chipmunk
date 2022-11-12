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

package chipmunk.compiler.parser;

import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PatternRecognizer<S, T, R> implements Function<S, R> {

    protected final Supplier<S> supplier;
    protected final TokenStream tokens = new TokenStream();
    protected final Set<TokenType> ignore = new HashSet<>();
    protected final List<Pattern<S, T, R>> patterns = new ArrayList<>();

    public R matchAll(){
        for(var pattern : patterns){
            var ast = match(pattern);
            if(ast != null){
                return ast;
            }
        }
        return null;
    }

    public <T extends Throwable> R matchAll(Supplier<T> noneMatch) throws T {
        var ast = matchAll();
        if(ast == null){
            throw noneMatch.get();
        }
        return ast;
    }

    public R match(Pattern<S, T, R> pattern){
        var index = tokens.mark();
        for(var type : pattern.pattern()){
            var token = tokens.get();
            if(!token.type().equals(type) && !ignore.contains(token.type())){
                tokens.rewind();
                return null;
            }
        }
        return pattern.action().apply(new TokenSequence(tokens, index));
    }

    public PatternRecognizer<S, T, R> define(Pattern<S, T, R> p){
        patterns.add(p);
        return this;
    }

    @Override
    public R apply(S source) {
        return matchAll();
    }
}
