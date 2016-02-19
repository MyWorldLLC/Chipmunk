package chipmunk.modules.lang;

public class CBooleanType extends CType {
	
	protected CBoolean tempValue;
	
	public CBooleanType(){
		super("Boolean");
		tempValue = new CBoolean();
		tempValue.type = this;
		tempValue.namespace.setVariable("type", this);
	}
	
	public CBoolean getTemp(){
		return tempValue;
	}

}
