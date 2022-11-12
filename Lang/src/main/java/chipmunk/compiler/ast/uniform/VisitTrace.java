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

package chipmunk.compiler.ast.uniform;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class VisitTrace {

    protected final Deque<AstNode> trace;

    public VisitTrace(){
        trace = new ArrayDeque<>();
    }

    public void enter(AstNode ast){
        trace.push(ast);
    }

    public void exit(){
        trace.pop();
    }

    public AstNode findFirst(Predicate<AstNode> p){
        return trace.stream().filter(p).findFirst().orElse(null);
    }

    public List<AstNode> findAll(Predicate<AstNode> p){
        return trace.stream().filter(p).toList();
    }

    public <T> T findFirst(Function<AstNode, T> extractor){
        return findFirst(extractor, Objects::nonNull);
    }

    public <T> T findFirst(Function<AstNode, T> extractor, Predicate<T> filter){
        return filteredStream(extractor, filter)
                .findFirst()
                .orElse(null);
    }

    public <T> List<T> findAll(Function<AstNode, T> extractor){
        return findAll(extractor, Objects::nonNull);
    }

    public <T> List<T> findAll(Function<AstNode, T> extractor, Predicate<T> filter){
        return filteredStream(extractor, filter)
                .toList();
    }

    public <T> Stream<T> filteredStream(Function<AstNode, T> extractor, Predicate<T> filter){
        return trace.stream()
                .map(extractor)
                .filter(filter);
    }

}
