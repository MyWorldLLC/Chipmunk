package chipmunk.modules.lang;


public class CInt extends CObject {

	protected int intValue;
	
	public CInt(){
		super();
		intValue = 0;
	}
	
	public int getValue(){
		return intValue;
	}
	
	public void setValue(int value){
		intValue = value;
	}
	
}
