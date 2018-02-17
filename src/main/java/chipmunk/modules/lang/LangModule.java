package chipmunk.modules.lang;

public class LangModule extends CModule {

	public LangModule(){
		super("chipmunk.lang");
		
		CNull nullObject = new CNull();
		nullObject.getType().setModule(this);
		namespace.setAttribute("null", CNullType.nullObject);
		
		CStringType stringType = new CStringType();
		stringType.setModule(this);
		namespace.setAttribute("String", stringType);
		
		CFloatType floatType = new CFloatType();
		floatType.setModule(this);
		namespace.setAttribute("float", floatType);
		
		CIntType intType = new CIntType();
		intType.setModule(this);
		namespace.setAttribute("int", intType);
		
		CBooleanType boolType = new CBooleanType();
		boolType.setModule(this);
		namespace.setAttribute("boolean", boolType);
		
		CMethodType methodType = new CMethodType();
		methodType.setModule(this);
		namespace.setAttribute("Method", methodType);
	}
	
}