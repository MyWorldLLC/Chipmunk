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

public class IfElseNode extends AstNode {
	
	protected boolean hasElse;
	
	public IfElseNode(){
		super(NodeType.IF_ELSE);
		hasElse = false;
	}
	
	public BlockNode getElseBranch(){
		if(hasElse){
			return (BlockNode) children.get(children.size() - 1);
		}else{
			return null;
		}
	}
	
	public void setElseBranch(BlockNode branch){
		if(branch != null){
			if(hasElse){
				children.remove(children.size() - 1);
			}
			super.addChild(branch);
			hasElse = true;
		}else{
			if(hasElse){
				children.remove(children.size() - 1);
			}
			hasElse = false;
		}
	}
	
	public void addGuardedBranch(GuardedNode child){
		if(!hasElse){
			super.addChild(child);
		}else{
			children.add(children.size() - 2, child);
		}
	}
	
	public void addGuardedBranches(GuardedNode... children){
		if(!hasElse){
			super.addChildren(children);
		}else{
			for(AstNode child : children){
				this.children.add(this.children.size() - 2, child);
			}
		}
	}

}
