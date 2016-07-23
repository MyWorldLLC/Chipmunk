package chipmunk.modules.lang;

import java.util.HashMap;
import java.util.Map;

public class CMethod extends CObject {
	
	protected CMethod[] outer;
	protected CObject[] locals;
	protected int argCount;
	protected Map<String, Integer> localNames;
	
	protected byte[] instructions;
	
	public CMethod(){
		super();
		localNames = new HashMap<String, Integer>();
	}

	public CMethod[] getOuter(){
		return outer;
	}
	
	public void setOuter(CMethod[] outer){
		this.outer = outer;
	}
	
	public CObject[] getLocals(){
		return locals;
	}
	
	public void setLocals(CObject[] locals){
		this.locals = locals;
	}
	
	public int getArgCount(){
		return argCount;
	}
	
	public void setArgCount(int count){
		argCount = count;
	}
	
	public byte[] getInstructions() {
		return instructions;
	}

	public void setInstructions(byte[] instructions) {
		this.instructions = instructions;
	}

	public int getLocalIndex(String name){
		
		Integer index = localNames.get(name);
		
		if(index != null){
			return index;
		}else{
			return -1;
		}
		
	}
	
	public void setLocalIndex(String name, int index){
		localNames.put(name, index);
	}
	
}