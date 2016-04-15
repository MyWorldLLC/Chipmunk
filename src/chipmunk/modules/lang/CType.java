package chipmunk.modules.lang;


public abstract class CType extends CObject {

	protected String name;
	protected Module module;
	
	public CType(){
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
	
	public Module getModule(){
		return module;
	}
	
	public void setModule(Module typeModule){
		module = typeModule;
		module.getNamespace().setVariable(name, this);
	}
	
	public abstract CObject instance();
	
}
