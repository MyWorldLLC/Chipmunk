package chipmunk.modules.reflectiveruntime;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CModule {
	
	private final List<Object> constants;
	private final Map<String, Object> attributes;
	
	public CModule(List<Object> constantPool){
		constants = constantPool;
		attributes = new HashMap<String, Object>();
	}
	
	public List<Object> getConstantsUnmodifiable(){
		return Collections.unmodifiableList(constants);
	}
	
	
}
