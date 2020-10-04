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

package chipmunk;

import java.util.Arrays;

public class OperandStack {
	private Object[] stack;
	private int insertionIndex;
	
	public OperandStack() {
		stack = new Object[16];
		insertionIndex = 0;
	}
	
	public void pushArgs(Object[] args){
		// push arguments right -> left
		for(int i = args.length - 1; i >= 0; i--){
			this.push(args[i]);
		}
	}

	public void popArgs(int argCount, Object[] args){
		// pop count args right -> left, leaving padding at the front of the array if it
		// is larger than needed
		for(int i = args.length - 1, j = 0; j < argCount; i--, j++){
			args[i] = pop();
		}
	}
	
	public void push(Object obj) {
		try {
			stack[insertionIndex] = obj;
			insertionIndex++;
		}catch(ArrayIndexOutOfBoundsException e) {
			stack = Arrays.copyOf(stack, stack.length + 128);
			this.push(obj);
		}
	}

	public Object pop() {
		insertionIndex--;
		return stack[insertionIndex];
	}

	public Object peek() {
		return stack[insertionIndex - 1];
	}

	public void dup(int index) {
		this.push(stack[insertionIndex - (index + 1)]);
	}

	public void swap(int index1, int index2) {
		int stackIndex1 = insertionIndex - (index1 + 1);
		int stackIndex2 = insertionIndex - (index2 + 1);

		Object obj1 = stack[stackIndex1];
		Object obj2 = stack[stackIndex2];

		stack[index1] = obj2;
		stack[index2] = obj1;
	}

	public int getStackDepth(){
		return insertionIndex + 1;
	}

	public int mark() {
		return insertionIndex;
	}

	public boolean verifyMark(int mark){
		return mark == insertionIndex;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Stack depth: " + insertionIndex);
		sb.append('\n');
		
		for(int i = 0; i < insertionIndex; i++) {
			sb.append("  ");
			sb.append(stack[insertionIndex] != null ? stack[insertionIndex].toString() : null);
			sb.append('\n');
		}
		
		return sb.toString();
	}
}