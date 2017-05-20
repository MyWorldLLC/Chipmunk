package chipmunk.modules.lang;


public abstract class CType extends CObject {

	protected String name;
	protected CModule module;
	
	public CType(){
		super();
		name = "";
		type = this;
	}
	
	public CType(String typeName){
		name = typeName;
		type = this;
	}
	
	public void setName(String typeName){
		name = typeName;
	}
	
	public String getName(){
		return name;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public abstract CObject instance();
	
	public void setModule(CModule typeModule){
		module = typeModule;
		module.getNamespace().setVariable(name, this);
	}
	
}
