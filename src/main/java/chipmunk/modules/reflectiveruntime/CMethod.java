package chipmunk.modules.reflectiveruntime;

import java.util.List;

import chipmunk.ChipmunkVM;
import chipmunk.reflectors.VMOperator;

public class CMethod implements VMOperator {
	protected int argCount;
	protected int defaultArgCount;
	protected int localCount;
	
	protected byte[] instructions;
	protected List<Object> constantPool;
	
	public CMethod(){
		super();
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
		localCount = count;
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
		return vm.dispatch(instructions, paramCount.intValue(), localCount, constantPool).getObject();
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
