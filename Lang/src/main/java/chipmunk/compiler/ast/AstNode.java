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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AstNode {

	protected AstNode parent;
	protected final List<AstNode> children;
	protected final NodeType type;
	protected final Token token;
	protected Symbol symbol;
	protected SymbolTable symbols;

	public AstNode(NodeType type){
		this(type, null);
	}
	
	public AstNode(NodeType type, Token token){
		this.type = type;
		this.token = token;
		children = new ArrayList<>();
		if(type.isBlock()){
			symbols = new SymbolTable(type.getScope());
			symbols.setNode(this);
		}
	}
	
	public AstNode(NodeType type, Token token, AstNode... children){
		this(type, token);
		this.children.addAll(Arrays.asList(children));
	}

	public NodeType getType(){
		return type;
	}

	public Token getToken(){
		return token;
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
		return Collections.unmodifiableList(children);
	}

	public AstNode getLeft(){
		if(hasChildren()){
			return children.get(0);
		}
		return null;
	}

	public AstNode getRight(){
		if(hasChildren() && !isUnary()){
			return children.get(children.size() - 1);
		}
		return null;
	}

	public AstNode getChild(){
		return getLeft();
	}

	public AstNode getChild(int index){
		return children.get(index);
	}

	public boolean isBinary(){
		return children.size() == 2;
	}

	public boolean isUnary(){
		return children.size() == 1;
	}
	
	public boolean hasChildren(){
		return children.size() > 0;
	}
	
	public void addChild(AstNode child){
		children.add(child);
		child.setParent(this);
	}

	public void addChild(int index, AstNode child){
		children.add(index, child);
		child.setParent(this);
	}

	public AstNode withChild(AstNode child){
		addChild(child);
		return this;
	}

	public void replaceChild(int index, AstNode child){
		children.set(index, child).setParent(null);
		child.setParent(this);
	}

	public void removeChild(AstNode child){
		if(children.remove(child)){
			child.setParent(null);
		}
	}

	public void removeChild(int index){
		children.remove(index).setParent(null);
	}
	
	protected void addChildren(AstNode... children){
		for(AstNode child : children){
			addChild(child);
		}
	}
	
	protected void addChildFirst(AstNode child){
		children.add(0, child);
		child.setParent(this);
	}

	protected void setParent(AstNode parent){
		this.parent = parent;
	}

	public AstNode getParent(){
		return parent;
	}
	
	public int getTokenIndex(){
		return token != null ? token.index() : 0;
	}
	
	public int getLineNumber() {
		return token != null ? token.line() : 0;
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

	public boolean is(NodeType... types)
	{
		return Arrays.asList(types).contains(type);
	}

	public <T> T requireType(NodeType type, Supplier<T> f){
		if(is(type)){
			return f.get();
		}
		throw new IllegalArgumentException("Required node type %s, got %s".formatted(type, getType()));
	}

	public void requireType(NodeType type, Runnable r){
		requireType(type, () -> {
			r.run();
			return null;
		});
	}

	public int childCount(){
		return children.size();
	}

	public String getDebugName(){
		return type.getDebugName().toLowerCase();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append('(');

		String debugSymbol = getDebugName();
		if(debugSymbol != null){
			builder.append(debugSymbol);
		}

		if(symbol != null){
			builder.append(' ');
			builder.append(symbol.getName());
		}else if(token != null){
			builder.append(' ');
			builder.append(token.text());
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
