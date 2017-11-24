package chipmunk.modules.reflectiveruntime;

import java.util.List;

public class CMethod {
	protected int argCount;
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
}
