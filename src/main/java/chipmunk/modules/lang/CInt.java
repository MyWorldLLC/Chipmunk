package chipmunk.modules.lang;


public class CInt extends CObject {

	protected int intValue;
	
	public CInt(){
		super();
		intValue = 0;
	}
	
	public CInt(int value){
		super();
		intValue = value;
	}
	
	public int getValue(){
		return intValue;
	}
	
	public void setValue(int value){
		intValue = value;
	}
	
	@Override
	public CObject __plus__(CObject other){
		
		if(other instanceof CInt){
			return new CInt(intValue + ((CInt) other).intValue);
		}else if(other instanceof CFloat){
			return new CFloat((float)intValue + ((CFloat) other).floatValue);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int + %s addition", other.getClass().getSimpleName()));
		}
	}
	
}
