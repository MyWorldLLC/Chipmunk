package chipmunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.ChipmunkVM.CallFrame;
import chipmunk.modules.runtime.CModule;

public class ChipmunkScript {
	
	protected Map<String, CModule> modules;
	protected List<Object> stack;
	protected Deque<CallFrame> frozenCallStack;
	protected Deque<CModule> initializationQueue;
	private boolean initialized;

	protected String entryModule;
	protected String entryMethod;
	protected Object[] entryArgs;
	
	public ChipmunkScript(){
		this(128);
	}
	
	public ChipmunkScript(int initialStackDepth){
		modules = new HashMap<>();
		stack = new ArrayList<>(initialStackDepth);
		frozenCallStack = new ArrayDeque<>();
		initializationQueue = new ArrayDeque<>();
		initialized = false;
	}
	
	public boolean isFrozen(){
		return !frozenCallStack.isEmpty();
	}
	public boolean isInitialized(){ return initialized; }

	protected void initialized(){
		initialized = true;
	}

	public void setEntryCall(String module, String method, Object... args){
		entryModule = module;
		entryMethod = method;
		entryArgs = args;
	}
	
	public void setEntryCall(String module, String method){
		setEntryCall(module, method, new Object[]{});
	}

	public String getEntryModule(){
		return entryModule;
	}

	public String getEntryMethod(){
		return entryMethod;
	}

	public Map<String, CModule> getModules(){
		return modules;
	}
	public void setModule(CModule module){
		modules.put(module.getName(), module);
	}
}
