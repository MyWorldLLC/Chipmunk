package chipmunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.modules.lang.CMethod;
import chipmunk.modules.lang.CModule;
import chipmunk.modules.lang.CObject;

public class ChipmunkContext {
	
	public class CallFrame {
		public final CMethod method;
		public final CObject next;
		public final int ip;
		public final CObject[] locals;
		
		public CallFrame(CMethod method, CObject next, int ip, CObject[] locals){
			this.method = method;
			this.next = next;
			this.ip = ip;
			this.locals = locals;
		}
	}

	protected Map<String, CModule> modules;
	protected List<CObject> stack;
	protected Deque<CallFrame> frozenCallStack;
	public volatile boolean interrupted;
	
	public ChipmunkContext(){
		modules = new HashMap<String, CModule>();
		// initialize operand stack to be 128 elements deep
		stack = new ArrayList<CObject>(128);
		
		frozenCallStack = new ArrayDeque<CallFrame>(128);
	}
	
	public CModule getModule(String name){
		return modules.get(name);
	}
	
	public CModule resolveModule(String name){
		// TODO - resolve module name, loading it if needed
		return null;
	}
	
	public void addModule(CModule module){
		modules.put(module.getName(), module);
	}
	
	public boolean removeModule(CModule module){
		
		CModule removed = modules.remove(module.getName());
		
		if(removed == null){
			return false;
		}else{
			return true;
		}
		
	}
	
	public void push(CObject obj){
		stack.add(obj);
	}
	
	public CObject pop(){
		return stack.remove(stack.size() - 1);
	}
	
	public void dup(int index){
		CObject obj = stack.get(stack.size() - (index + 1));
		stack.add(obj);
	}
	
	public void swap(int index1, int index2){
		int stackIndex1 = stack.size() - (index1 + 1);
		int stackIndex2 = stack.size() - (index2 + 1);
		
		CObject obj1 = stack.get(stackIndex1);
		CObject obj2 = stack.get(stackIndex2);
		
		stack.set(index1, obj2);
		stack.set(index2, obj1);
	}
	
	public void freeze(CMethod method, CObject next, int ip, CObject[] locals ){
		frozenCallStack.push(new CallFrame(method, next, ip, locals));
	}
	
	public CallFrame unfreezeNext(){
		return frozenCallStack.pop();
	}
	
	public void resume(){
		// TODO - if frozen call stack isn't empty,
		// get method at top and call it.
	}
	
}
