package chipmunk.modules.runtime;

import chipmunk.ExceptionBlock;

public class CMethodCode {
	protected int argCount;
	protected int defaultArgCount;
	protected int localCount;
	
	protected byte[] instructions;
	protected Object[] constantPool;
	protected ExceptionBlock[] exceptionTable;
	
	protected Object[] callCache;
	
	protected CModule module;
	
	protected int callSiteCount;
	
	public CMethodCode(){
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
	
	public void setExceptionTable(ExceptionBlock[] table) {
		exceptionTable = table;
	}
	
	public ExceptionBlock[] getExceptionTable() {
		return exceptionTable;
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
	
	public CModule getModule(){
		return module;
	}
	
	public void setModule(CModule module){
		this.module = module;
	}
	
}
