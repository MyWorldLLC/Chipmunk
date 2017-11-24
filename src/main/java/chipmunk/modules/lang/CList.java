package chipmunk.modules.lang;

import java.util.ArrayList;
import java.util.List;

public class CList extends CObject {
	
	protected List<CObject> contents;
	
	public CList(){
		super();
		contents = new ArrayList<CObject>();
	}
	
	@Override
	public CObject __getAt__(CObject index){
		int listIndex = ((CInt) index).getValue();
		// TODO - wrap out-of-bounds exception
		return contents.get(listIndex);
	}
	
	@Override
	public CObject __setAt__(CObject index, CObject value){
		int listIndex = ((CInt) index).getValue();
		// TODO - wrap out-of-bounds exception
		return contents.set(listIndex, value);
	}

}
