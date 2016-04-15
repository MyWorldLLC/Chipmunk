package chipmunk.modules.lang;

public class CStringType extends CType {
	
	protected CString tempString;
	
	public CStringType(){
		super("String");
		tempString = new CString();
		tempString.type = this;
		tempString.namespace.setVariable("type", this);
	}
	
	public CString getTemp(){
		return tempString;
	}
	
	public CObject instance(){
		return new CString();
	}

}
