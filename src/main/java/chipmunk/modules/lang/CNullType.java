package chipmunk.modules.lang;


public class CNullType extends CType {

	public static final CNull nullObject = new CNull();
	
	public CNullType(){
		super("Null");
	}
	
	public CObject instance() {
		// shared instance of null object
		return nullObject;
	}

}
