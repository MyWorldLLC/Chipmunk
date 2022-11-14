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

import chipmunk.util.SeekableSequence;

import java.util.List;
import java.util.function.Function;

public class PatternFactory<S, SEQ extends SeekableSequence<S>, T, R> {

    public record PartialPattern<S, SEQ extends SeekableSequence<S>, T, R>(List<T> pattern){

        public Pattern<S, SEQ, T, R> then(Function<SEQ, R> action){
            return new Pattern<>(pattern, action);
        }

    }

    public PartialPattern<S, SEQ, T, R> when(T... pattern){
        return new PartialPattern<>(List.of(pattern));
    }
}
