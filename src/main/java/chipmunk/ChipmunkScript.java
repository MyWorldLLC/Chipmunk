package chipmunk;

import java.util.*;

import chipmunk.ChipmunkVM.CallFrame;
import chipmunk.modules.runtime.CModule;

public class ChipmunkScript {

	protected List<ModuleLoader> loaders;
	protected Map<String, CModule> modules;

	protected final List<Object> tags;

	protected Deque<CallFrame> frozenCallStack;
	private boolean initialized;

	protected String entryModule;
	protected String entryMethod;
	protected Object[] entryArgs;
	
	public ChipmunkScript(){
		this(128);
	}
	
	public ChipmunkScript(int initialStackDepth){
		loaders = new ArrayList<>();
		modules = new HashMap<>();
		tags = new ArrayList<>();
		frozenCallStack = new ArrayDeque<>();
		initialized = false;
	}
	
	public boolean isFrozen(){
		return !frozenCallStack.isEmpty();
	}
	public boolean isInitialized(){ return initialized; }

	protected void markInitialized(){
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

	public List<ModuleLoader> getLoaders(){
		return loaders;
	}

	public void setLoaders(List<ModuleLoader> loaders){
		this.loaders = loaders;
	}

	public synchronized void tag(Object tag){
		tags.add(tag);
	}

	public synchronized void removeTag(Object tag){
		tags.remove(tag);
	}
	public synchronized <T> T getTag(Class<?> tagType){
		for(Object o : tags){
			if(tagType.isInstance(o)){
				return (T) o;
			}
		}
		return null;
	}
	public synchronized List<Object> getTagsUnmodifiable(){
		return Collections.unmodifiableList(tags);
	}
}
