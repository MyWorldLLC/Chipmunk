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

import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AstNode {
	
	protected final List<AstNode> children;
	protected final NodeType type;
	protected final Token token;
	protected Symbol symbol;
	protected SymbolTable symbols;
	protected int beginTokenIndex;
	protected int endTokenIndex;
	protected int lineNumber;

	// TODO - remove this. It's a temporary workaround
	// to allow compiling/testing while overhauling the AST/parser.
	public AstNode(){
		this(NodeType.MODULE);
	}

	// TODO - see above
	public AstNode(AstNode... children){
		this(null, null, children);
	}

	public AstNode(NodeType type){
		this(type, null);
	}
	
	public AstNode(NodeType type, Token token){
		this.type = type;
		this.token = token;
		children = new ArrayList<>();
	}
	
	public AstNode(NodeType type, Token token, AstNode... children){
		this(type, token);
		this.children.addAll(Arrays.asList(children));
	}

	public Symbol getSymbol(){
		return symbol;
	}

	public void setSymbol(Symbol symbol){
		this.symbol = symbol;
	}

	public SymbolTable getSymbolTable(){
		return symbols;
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
