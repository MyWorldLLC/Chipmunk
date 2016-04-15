package chipmunk.modules.lang;

public class CIntType extends CType {

	protected CInt tempValue;
	
	public CIntType(){
		super("Int");
		tempValue = new CInt();
		tempValue.type = this;
		tempValue.namespace.setVariable("type", this);
	}
	
	public CInt getTemp(){
		return tempValue;
	}
	
	public CObject instance(){
		return new CInt();
	}
}
