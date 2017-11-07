package chipmunk.modules.lang;

public class LangModule extends CModule {

	public LangModule(){
		super("chipmunk.lang");
		
		CNull nullObject = new CNull();
		nullObject.getType().setModule(this);
		namespace.setVariable("null", CNullType.nullObject);
		
		CStringType stringType = new CStringType();
		stringType.setModule(this);
		namespace.setVariable("String", stringType);
		
		CFloatType floatType = new CFloatType();
		floatType.setModule(this);
		namespace.setVariable("float", floatType);
		
		CIntType intType = new CIntType();
		intType.setModule(this);
		namespace.setVariable("int", intType);
		
		CBooleanType boolType = new CBooleanType();
		boolType.setModule(this);
		namespace.setVariable("boolean", boolType);
		
		CMethodType methodType = new CMethodType();
		methodType.setModule(this);
		namespace.setVariable("Method", methodType);
	}
	
}