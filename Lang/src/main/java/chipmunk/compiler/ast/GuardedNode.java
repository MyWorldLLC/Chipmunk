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

public class GuardedNode extends BlockNode {
	
	protected boolean hasGuard;
	
	public GuardedNode(){
		super();
		hasGuard = false;
	}
	
	public GuardedNode(AstNode guard, AstNode... children){
		super(children);
		hasGuard = false;
		setGuard(guard);
	}
	
	public void setGuard(AstNode guard){
		
		if(hasGuard){
			children.remove(0);
			hasGuard = false;
		}
		
		if(guard != null){
			addChildFirst(guard);
			hasGuard = true;
		}
	}
	
	public AstNode getGuard(){
		if(hasGuard){
			return children.get(0);
		}else{
			return null;
		}
	}
	
	public boolean hasGuard(){
		return hasGuard;
	}
	
	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	public void addChildren(AstNode... children){
		super.addChildren(children);
	}

}
