package chipmunk.modules.reflectiveruntime;

public class CFloat {
	
	private final float value;

	public CFloat(float value){
		this.value = value;
	}
	
	public float getValue(){
		return value;
	}
	
	public int intValue(){
		return (int) value;
	}
	
	public float floatValue(){
		return value;
	}
}
