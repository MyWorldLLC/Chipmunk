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