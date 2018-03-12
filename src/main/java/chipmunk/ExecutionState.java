package chipmunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.ChipmunkVM.CallFrame;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CModule;

public class ExecutionState {
	
	protected Map<String, CModule> modules;
	protected List<Object> stack;
	protected Deque<CallFrame> frozenCallStack;
	protected Deque<CModule> initializationQueue;
	
	protected CMethod entryMethod;
	protected Object[] entryArgs;
	
	public ExecutionState(){
		this(new HashMap<String, CModule>(), 128);
	}
	
	public ExecutionState(Map<String, CModule> modules){
		this(modules, 128);
	}
	
	public ExecutionState(Map<String, CModule> modules, int initialStackDepth){
		this.modules = modules;
		stack = new ArrayList<Object>(initialStackDepth);
		frozenCallStack = new ArrayDeque<CallFrame>();
		initializationQueue = new ArrayDeque<CModule>();
	}
	
	public boolean isFrozen(){
		return !frozenCallStack.isEmpty();
	}
	
}
