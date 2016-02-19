package chipmunk.modules.lang;


public class LangModule extends Module {

	public LangModule(){
		super("chipmunk.lang");
		
		Null nullObject = new Null();
		nullObject.getType().setModule(this);
		namespace.setVariable("Null", nullObject);
		
		CStringType stringType = new CStringType();
		namespace.setVariable("String", stringType);
	}
	
}