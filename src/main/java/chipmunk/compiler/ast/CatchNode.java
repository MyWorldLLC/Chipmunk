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

import chipmunk.compiler.Token;

public class CatchNode extends BlockNode {
	
	protected boolean hasExceptionName;
	protected boolean hasExceptionType;
	
	protected Token exceptionType;
	
	public CatchNode(){
		hasExceptionName = false;
	}
	
	public void setExceptionName(Token name){
		if(hasExceptionName){
			if(hasExceptionType){
				children.remove(1);
			}else{
				children.remove(0);
			}
		}
		if(name != null){
			if(hasExceptionType){
				children.add(1, new VarDecNode(new IdNode(name)));
			}else{
				children.add(0, new VarDecNode(new IdNode(name)));
			}
			hasExceptionName = true;
		}else{
			hasExceptionName = false;
		}
	}
	
	public void setExceptionType(Token typeName){
		if(hasExceptionType){
			children.remove(0);
		}
		
		if(typeName != null){
			children.add(0, new IdNode(typeName));
			hasExceptionType = true;
		}else{
			hasExceptionType = false;
		}
	}
	
	public boolean hasExceptionName(){
		return hasExceptionName;
	}
	
	public boolean hasExceptionType(){
		return hasExceptionType;
	}
	
	public IdNode getExceptionType(){
		if(hasExceptionType){
			return (IdNode) children.get(0);
		}
		return null;
	}
	
	public VarDecNode getExceptionName(){
		if(hasExceptionName){
			if(hasExceptionType){
				return (VarDecNode) children.get(1);
			}else{
				return (VarDecNode) children.get(0);
			}
		}
		return null;
	}

}
