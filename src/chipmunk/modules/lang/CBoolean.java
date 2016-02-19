package chipmunk.modules.lang;

public class CBoolean extends CObject {

	protected boolean boolValue;
	
	public CBoolean(){
		super();
		boolValue = false;
	}
	
	public CBoolean(boolean value){
		super();
		boolValue = value;
	}
	
	public boolean getValue(){
		return boolValue;
	}
	
	public void setValue(boolean value){
		boolValue = value;
	}
	
}
