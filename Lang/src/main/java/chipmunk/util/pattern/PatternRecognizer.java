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
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class PatternRecognizer<S, V extends Visitor<S>, E, T, R> {

    protected final Function<S, E> extractor;
    protected final BiPredicate<E, T> matcher;

    protected final Set<T> ignore = new HashSet<>();
    protected final List<Pattern<S, V, T, R>> patterns = new ArrayList<>();

    public PatternRecognizer(Function<S, E> extractor){
        this(extractor, Object::equals);
    }

    public PatternRecognizer(Function<S, E> extractor, BiPredicate<E, T> matcher){
        this.extractor = extractor;
        this.matcher = matcher;
    }

    @SuppressWarnings("unchecked")
    public PatternRecognizer(BiPredicate<E, T> matcher){
        this(s -> (E) s, matcher);
    }

    @SafeVarargs
    public final PatternRecognizer<S, V, E, T, R> ignore(T... t){
        ignore.addAll(Arrays.asList(t));
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

    public boolean testAll(V source){
        for(var pattern : patterns){
            if(test(source, pattern)){
                return true;
            }
        }
        return false;
    }

    public boolean test(V source, Pattern<S, V, T, R> pattern){
        var dup = source.duplicate();
        for(var t : pattern.pattern()){
            var s = nextMatchable(dup);
            if(!matcher.test(s, t)){
                return false;
            }
        }
        return true;
    }

    public <EX extends Throwable> R matchAll(V source, Supplier<EX> noneMatch) throws EX {
        var ast = matchAll(source);
        if(ast == null){
            throw noneMatch.get();
        }
        return ast;
    }

    public R match(V source, Pattern<S, V, T, R> pattern){
        if(test(source, pattern)){
            return pattern.action().apply(source);
        }
        return null;
    }

    protected E nextMatchable(Visitor<S> source){
        while(source.hasMore()){
            var s = extractor.apply(source.get());
            if(ignore.stream().noneMatch(i -> matcher.test(s, i))){
                return s;
            }
        }
        return null;
    }

    public PatternRecognizer<S, V, E, T, R> define(Pattern<S, V, T, R> p){
        patterns.add(p);
        return this;
    }

    public PatternFactory<S, V, T, R> factory(){
        return new PatternFactory<>();
    }
}
