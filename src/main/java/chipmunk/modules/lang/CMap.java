package chipmunk.modules.lang;

import java.util.HashMap;
import java.util.Map;

public class CMap extends CObject {
	
	protected Map<CObject, CObject> contents;
	
	public CMap(){
		super();
		contents = new HashMap<CObject, CObject>();
	}
	
	@Override
	public CObject __getAt__(CObject index){
		return contents.get(index);
	}
	
	@Override
	public CObject __setAt__(CObject index, CObject value){
		CObject previous = contents.put(index, value);
		return previous != null ? previous : CNullType.nullObject;
	}

}
