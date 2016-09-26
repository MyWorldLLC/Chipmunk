package chipmunk.modules.lang;

import chipmunk.modules.lang.CObject;

public class CFloat extends CObject {
	
	protected float floatValue;
	
	public CFloat(){
		super();
		floatValue = 0.0f;
	}
	
	public CFloat(float value){
		super();
		floatValue = value;
	}
	
	public float getValue(){
		return floatValue;
	}
}
