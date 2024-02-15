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

package chipmunk.compiler.ast.patterns;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.NodeType;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public record NodePattern(NodeFetcher fetcher, Predicate<AstNode> predicate) {

    public static final BiPredicate<AstNode, NodePattern> NODE_MATCHER =
            (node, pattern) -> pattern.predicate().test(pattern.fetcher().apply(node));

    public static NodePattern type(NodeFetcher fetcher, NodeType type){
        return new NodePattern(fetcher, n -> type.equals(n.getNodeType()));
    }

}
