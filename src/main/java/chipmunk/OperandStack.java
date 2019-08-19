package chipmunk;

import java.util.Arrays;

public class OperandStack {
	public Object[] stack;
	public int stackIndex;
	
	public OperandStack() {
		stack = new Object[16];
		stackIndex = 0;
	}
	
	public void pushArgs(Object[] args){
		// push arguments right -> left
		for(int i = args.length - 1; i >= 0; i--){
			this.push(args[i]);
		}
	}
	
	public void push(Object obj) {
		try {
			stack[stackIndex] = obj;
			stackIndex++;
		}catch(ArrayIndexOutOfBoundsException e) {
			stack = Arrays.copyOf(stack, stack.length + 128);
			this.push(obj);
		}
	}

	public Object pop() {
		stackIndex--;
		return stack[stackIndex];
	}

	public Object peek() {
		return stack[stackIndex - 1];
	}

	public void dup(int index) {
		stack[stackIndex] = stack[stackIndex - (index + 1)];
		stackIndex++;
	}

	public void swap(int index1, int index2) {
		int stackIndex1 = stackIndex - (index1 + 1);
		int stackIndex2 = stackIndex - (index2 + 1);

		Object obj1 = stack[stackIndex1];
		Object obj2 = stack[stackIndex2];

		stack[index1] = obj2;
		stack[index2] = obj1;
	}
}