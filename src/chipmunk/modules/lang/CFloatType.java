package chipmunk.modules.lang;

public class CFloatType extends CType {

	protected CFloat tempValue;
	
	public CFloatType(){
		super("Float");
		tempValue = new CFloat();
		tempValue.type = this;
		tempValue.namespace.setVariable("type", this);
	}
	
	public CFloat getTemp(){
		return tempValue;
	}
	
	public CObject instance(){
		return new CFloat();
	}
	
}
