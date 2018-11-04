package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;

public class CMethod implements RuntimeObject {
	protected int argCount;
	protected int defaultArgCount;
	protected int localCount;
	
	protected byte[] instructions;
	protected Object[] constantPool;
	
	protected Object[] callCache;
	
	protected Object self;
	protected CModule module;
	
	protected int callSiteCount;
	
	public CMethod(){
		super();
		self = CNull.instance();
		localCount = 1;
	}
	
	public int getArgCount(){
		return argCount;
	}
	
	public void setArgCount(int count){
		argCount = count;
	}
	
	public int getDefaultArgCount(){
		return defaultArgCount;
	}
	
	public void setDefaultArgCount(int count){
		defaultArgCount = count;
	}
	
	public int getLocalCount(){
		return localCount;
	}
	
	public void setLocalCount(int count){
		localCount = count + 1; // + 1 for self reference
	}
	
	public void setConstantPool(Object[] constantPool){
		this.constantPool = constantPool;
	}
	
	public Object[] getConstantPool(){
		return constantPool;
	}
	
	public void setCallSiteCount(int count) {
		callSiteCount = count;
	}
	
	public int getCallSiteCount() {
		return callSiteCount;
	}
	
	public void setCode(byte[] codeSegment) {
		instructions = codeSegment;
		callCache = new Object[codeSegment.length];
	}
	
	public byte[] getCode(){
		return instructions;
	}
	
	public Object[] getCallCache(){
		return callCache;
	}
	
	public Object call(ChipmunkVM vm, Byte paramCount) {
		return vm.dispatch(this, paramCount.intValue());
	}
	
	public Object getSelf(){
		return self;
	}
	
	public void bind(Object self){
		this.self = self;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public void setModule(CModule module){
		this.module = module;
	}
	
	public CMethod duplicate(ChipmunkVM vm){
		vm.traceMem(12); // integer sizes
		
		CMethod method = new CMethod();
		method.setModule(module);
		method.setCode(instructions);
		method.setConstantPool(constantPool);
		method.setArgCount(argCount);
		method.setDefaultArgCount(defaultArgCount);
		method.setLocalCount(localCount);
		method.bind(self);
		
		return method;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.getClass().getSimpleName());
		builder.append("[Locals: ");
		builder.append(localCount);
		builder.append(", Args: ");
		builder.append(argCount);
		builder.append(", Def. Args: ");
		builder.append(defaultArgCount);
		builder.append("]");
		
		return builder.toString();
	}
}
