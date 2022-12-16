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

	protected boolean hasIterator;
	
	public ForNode(){
		super(NodeType.FOR);
		hasIterator = false;
	}
	
	public boolean hasIterator(){
		return hasIterator;
	}

	public void setIterator(IteratorNode iterator){
		if(hasIterator){
			if(iterator != null){
				children.set(0, iterator);
				hasIterator = true;
			}else{
				children.remove(0);
				hasIterator = false;
			}
		}else{
			children.add(0, iterator);
			hasIterator = true;
		}
	}

	public IteratorNode getIterator() {
		if (hasIterator) {
			return (IteratorNode) children.get(0);
		} else {
			return null;
		}
	}

}
