package chipmunk.modules.lang;

import chipmunk.Namespace;

public class CClassType extends CType {

	protected Namespace typeFields;
	public CClassType(){
		super();
		typeFields = new Namespace();
	}
	
	public CClassType(String name){
		super(name);
		typeFields = new Namespace();
	}
	
	public void setField(String name, CObject value){
		typeFields.setVariable(name, value);
	}
	
	public CObject getField(String name){
		return typeFields.getObject(name);
	}
}
