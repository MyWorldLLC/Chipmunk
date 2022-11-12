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

import java.util.List;
import java.util.function.Function;

public record Pattern<S, T, R>(List<T> pattern, Function<S, R> action) {

    public static <S, T, R> Pattern<S, T, R> of(List<T> pattern, Function<S, R> action){
        return new Pattern<>(pattern, action);
    }

    @SafeVarargs
    public static <S, T, R> Pattern<S, T, R> when(T... pattern){
        return new Pattern<>(List.of(pattern), null);
    }

    public Pattern<S, T, R> then(Function<S, R> action){
        return Pattern.of(pattern, action);
    }

}
