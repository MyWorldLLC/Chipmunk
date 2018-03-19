package chipmunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.ChipmunkVM.CallFrame;
import chipmunk.modules.reflectiveruntime.CModule;

public class ChipmunkScript {
	
	protected Map<String, CModule> modules;
	protected List<Object> stack;
	protected Deque<CallFrame> frozenCallStack;
	protected Deque<CModule> initializationQueue;
	
	protected String entryModule;
	protected String entryMethod;
	protected Object[] entryArgs;
	
	public ChipmunkScript(){
		this(128);
	}
	
	public ChipmunkScript(int initialStackDepth){
		modules = new HashMap<String, CModule>();
		stack = new ArrayList<Object>(initialStackDepth);
		frozenCallStack = new ArrayDeque<CallFrame>();
		initializationQueue = new ArrayDeque<CModule>();
	}
	
	public boolean isFrozen(){
		return !frozenCallStack.isEmpty();
	}
	
	public void setEntryCall(String module, String method, Object... args){
		entryModule = module;
		entryMethod = method;
		entryArgs = args;
	}
	
	public void setEntryCall(String module, String method){
		setEntryCall(module, method, new Object[]{});
	}
	
	public Map<String, CModule> getModules(){
		return modules;
	}
}
