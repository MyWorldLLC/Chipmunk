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

public class ForNode extends BlockNode {
	
	protected boolean hasIter;
	protected boolean hasID;
	
	public ForNode(){
		hasIter = false;
		hasID = false;
	}
	
	public boolean hasIter(){
		return hasIter;
	}
	
	public boolean hasID(){
		return hasID;
	}
	
	public void setIter(AstNode iterExpr){
		if(iterExpr == null){
			if(hasIter && hasID){
				children.remove(1);
			}else if(hasIter && !hasID){
				children.remove(0);
			}
			hasIter = false;
		}else{
			if(hasIter && hasID){
				children.remove(1);
			}else if(hasIter && !hasID){
				children.remove(0);
			}
			if(!hasID){
				children.add(0, iterExpr);
			}else{
				children.add(1, iterExpr);
			}
			hasIter = true;
		}
	}
	
	public AstNode getIter(){
		if(hasIter && !hasID){
			return children.get(0);
		}else if(hasIter){
			return children.get(1);
			
		}else{
			return null;
		}
	}
	
	public void setID(VarDecNode id){
		if(id == null){
			if(hasID){
				children.remove(0);
			}
			hasID = false;
		}else{
			if(hasID){
				children.remove(0);
			}
			children.add(0, id);
			hasID = true;
		}
	}
	
	public VarDecNode getID(){
		if(hasID){
			return (VarDecNode) children.get(0);
		}else{
			return null;
		}
	}

}
