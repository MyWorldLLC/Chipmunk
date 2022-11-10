/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AstNode {
	
	protected List<AstNode> children;
	protected int beginTokenIndex;
	protected int endTokenIndex;
	protected int lineNumber;
	
	public AstNode(){
		children = new ArrayList<>();
	}
	
	public AstNode(AstNode... children){
		this();
		for(AstNode child : children){
			this.children.add(child);
		}
	}
	
	public List<AstNode> getChildren(){
		return children;
	}
	
	public boolean hasChildren(){
		return children.size() > 0;
	}
	
	protected void addChild(AstNode child){
		children.add(child);
	}
	
	protected void addChildren(AstNode... children){
		this.children.addAll(Arrays.asList(children));
	}
	
	protected void addChildFirst(AstNode child){
		children.add(0, child);
	}
	
	public int getBeginTokenIndex(){
		return beginTokenIndex;
	}
	
	public void setBeginTokenIndex(int index){
		beginTokenIndex = index;
	}
	
	public int getEndTokenIndex(){
		return beginTokenIndex;
	}
	
	public void setEndTokenIndex(int index){
		endTokenIndex = index;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setLineNumber(int line) {
		lineNumber = line;
	}

	public void visit(AstVisitor visitor){
		visitor.visit(this);
	}
	
	public void visitChildren(AstVisitor visitor){
		for(AstNode child : children){
			child.visit(visitor);
		}
	}

	public void visitChildrenRightLeft(AstVisitor visitor){
		for(int i = children.size() - 1; i >= 0; i--){
			children.get(i).visit(visitor);
		}
	}
	
	public void visitChildren(AstVisitor visitor, int startAt){
		for(int i = startAt; i < children.size(); i++){
			visitor.visit(children.get(i));
		}
	}

	public String getDebugName(){
		return null;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append('(');

		String debugSymbol = getDebugName();
		if(debugSymbol != null){
			builder.append(debugSymbol);
		}

		if(!children.isEmpty()){
			builder.append(' ');
		}

		builder.append(
				children.stream()
						.map(AstNode::toString)
						.collect(Collectors.joining(" "))
		);
		
		builder.append(')');
		return builder.toString();
	}

}
