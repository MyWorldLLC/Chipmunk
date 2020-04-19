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

public class TryCatchNode extends AstNode {

	protected boolean hasTry;
	protected boolean hasFinally;
	
	public TryCatchNode(){
		hasTry = false;
		hasFinally = false;
	}
	
	public boolean hasTryBlock(){
		return hasTry;
	}
	
	public void setTryBlock(TryNode tryBlock){
		if(tryBlock != null){
			if(hasTry){
				children.remove(0);
			}
			children.add(0, tryBlock);
			hasTry = true;
		}else{
			if(hasTry){
				children.remove(0);
			}
			hasTry = false;
		}
	}
	
	public void addCatchBlock(CatchNode catchBlock){
		if(hasFinally) {
			children.add(children.size() - 1, catchBlock);
		}else{
			children.add(catchBlock);
		}
	}
	
	public void setFinallyBlock(BlockNode finallyBlock) {
		if(finallyBlock != null){
			if(hasFinally){
				children.remove(children.size() - 1);
			}
			children.add(children.size() - 1, finallyBlock);
			hasFinally = true;
		}else{
			if(hasFinally){
				children.remove(children.size() - 1);
			}
			hasFinally = false;
		}
	}
	
}
