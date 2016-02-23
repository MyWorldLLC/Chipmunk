package chipmunk.modules.lang;


public class LangModule extends Module {

	public LangModule(){
		super("chipmunk.lang");
		
		Null nullObject = new Null();
		nullObject.getType().setModule(this);
		namespace.setVariable("Null", nullObject);
		
		CStringType stringType = new CStringType();
		stringType.setModule(this);
		namespace.setVariable("String", stringType);
		
		CFloatType floatType = new CFloatType();
		floatType.setModule(this);
		namespace.setVariable("Float", floatType);
		
		CIntType intType = new CIntType();
		intType.setModule(this);
		namespace.setVariable("Int", intType);
		
		CBooleanType boolType = new CBooleanType();
		boolType.setModule(this);
		namespace.setVariable("Bool", boolType);
		
		CMethodType methodType = new CMethodType();
		methodType.setModule(this);
		namespace.setVariable("Method", methodType);
	}
	
}