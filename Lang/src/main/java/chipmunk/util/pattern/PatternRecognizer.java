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

import chipmunk.util.Visitor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class PatternRecognizer<S, V extends Visitor<S>, T, R> implements Function<V, R> {
    protected final Function<S, T> extractor;
    protected final Set<T> ignore = new HashSet<>();
    protected final List<Pattern<S, V, T, R>> patterns = new ArrayList<>();

    public PatternRecognizer(Function<S, T> extractor){
        this.extractor = extractor;
    }

    public PatternRecognizer<S, V, T, R> ignore(T t){
        ignore.add(t);
        return this;
    }

    public R matchAll(V source){
        for(var pattern : patterns){
            var ast = match(source, pattern);
            if(ast != null){
                return ast;
            }
        }
        return null;
    }

    public <E extends Throwable> R matchAll(V source, Supplier<E> noneMatch) throws E {
        var ast = matchAll(source);
        if(ast == null){
            throw noneMatch.get();
        }
        return ast;
    }

    public R match(V source, Pattern<S, V, T, R> pattern){
        var dup = source.duplicate();
        for(var t : pattern.pattern()){
            var s = extractor.apply(dup.get());
            if(!s.equals(t) && !ignore.contains(s)){
                return null;
            }
        }
        return pattern.action().apply(source.duplicate());
    }

    public PatternRecognizer<S, V, T, R> define(Pattern<S, V, T, R> p){
        patterns.add(p);
        return this;
    }

    @Override
    public R apply(V source) {
        return matchAll(source);
    }
}
