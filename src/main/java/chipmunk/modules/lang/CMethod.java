package chipmunk.modules.lang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMethod extends CObject {
	
	protected CMethod outer;
	protected int argCount;
	protected int localCount;
	protected Map<String, Integer> localNames;
	
	protected byte[] instructions;
	protected CCode code;
	protected List<CObject> constantPool;
	
	public CMethod(){
		super();
		localNames = new HashMap<String, Integer>();
	}

	public CMethod getOuter(){
		return outer;
	}
	
	public void setOuter(CMethod outer){
		this.outer = outer;
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
	
	public CCode getCode(){
		return code;
	}
	
	public void setCode(CCode code){
		this.code = code;
		instructions = code.getCode();
	}
	
	public void setConstantPool(List<CObject> constantPool){
		this.constantPool = constantPool;
	}
	
	public List<CObject> getConstantPool(){
		return constantPool;
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