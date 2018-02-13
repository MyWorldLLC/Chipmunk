package chipmunk.modules.reflectiveruntime;

import java.util.List;

import chipmunk.ChipmunkVM;

public class CMethod implements RuntimeObject {
	protected int argCount;
	protected int defaultArgCount;
	protected int localCount;
	
	protected byte[] instructions;
	protected List<Object> constantPool;
	
	protected Object self;
	
	public CMethod(){
		super();
		self = new CNull();
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
	
	public void setConstantPool(List<Object> constantPool){
		this.constantPool = constantPool;
	}
	
	public List<Object> getConstantPool(){
		return constantPool;
	}
	
	public void setCode(byte[] codeSegment) {
		instructions = codeSegment;
	}
	
	public byte[] getCode(){
		return instructions;
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
	
	public CMethod duplicate(ChipmunkVM vm){
		vm.traceMem(12); // integer sizes
		
		CMethod method = new CMethod();
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
