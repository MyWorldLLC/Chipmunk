/*
 * Copyright (C) 2021 MyWorld, LLC
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

package chipmunk.doc.tree;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class DocNode {

    protected final String comment;
    protected final AstNode sourceNode;
    protected final String name;

    protected final List<DocNode> children;

    public DocNode(String comment, AstNode sourceNode, String name){
        this.comment = comment;
        this.sourceNode = sourceNode;
        this.name = name;

        children = new ArrayList<>();
    }

    public String getComment() {
        return comment;
    }

    public AstNode getSourceNode() {
        return sourceNode;
    }

    public String getName() {
        return name;
    }

    public List<DocNode> getChildren(){
        return children;
    }

    public boolean isModuleNode(){
        return sourceNode instanceof ModuleNode;
    }

    public boolean isClassNode(){
        return sourceNode.is(NodeType.CLASS);
    }

    public boolean isVarNode(){
        return sourceNode.is(NodeType.VAR_DEC);
    }

    public boolean isTraitVar(){
        return isVarNode() && sourceNode.getSymbol().isTrait();
    }
}
