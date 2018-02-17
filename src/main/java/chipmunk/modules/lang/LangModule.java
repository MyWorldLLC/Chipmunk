package chipmunk.modules.lang;

public class LangModule extends CModule {

	public LangModule(){
		super("chipmunk.lang");
		
		CNull nullObject = new CNull();
		nullObject.getType().setModule(this);
		namespace.set("null", CNullType.nullObject);
		
		CStringType stringType = new CStringType();
		stringType.setModule(this);
		namespace.set("String", stringType);
		
		CFloatType floatType = new CFloatType();
		floatType.setModule(this);
		namespace.set("float", floatType);
		
		CIntType intType = new CIntType();
		intType.setModule(this);
		namespace.set("int", intType);
		
		CBooleanType boolType = new CBooleanType();
		boolType.setModule(this);
		namespace.set("boolean", boolType);
		
		CMethodType methodType = new CMethodType();
		methodType.setModule(this);
		namespace.set("Method", methodType);
	}
	
}