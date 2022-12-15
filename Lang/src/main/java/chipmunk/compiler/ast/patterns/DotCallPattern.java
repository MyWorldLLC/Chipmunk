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

import chipmunk.compiler.ast.AstMatchVisitor;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.NodeType;
import chipmunk.util.pattern.PatternFactory;
import chipmunk.util.pattern.PatternRecognizer;

import static chipmunk.compiler.ast.patterns.NodeFetcher.*;
import static chipmunk.compiler.ast.patterns.NodePattern.*;
import static chipmunk.util.pattern.MatchResult.*;

public class DotCallPattern {

    public static void register(PatternRecognizer<AstNode, AstMatchVisitor, AstNode, NodePattern, Boolean> recognizer) {
        var f = new PatternFactory<AstNode, AstMatchVisitor, NodePattern, Boolean>();
        recognizer = new PatternRecognizer<>(NODE_MATCHER);

        recognizer.define(f.when(type(left(left(child(1))), NodeType.VAR_DEC)).then(success()));
    }

}
