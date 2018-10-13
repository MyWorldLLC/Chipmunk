package chipmunk.truffle.runtime;

public class Null {
	
	private static final Null instance = new Null();
	
	private Null() {}
	
	public static final Null instance() {
		return instance;
	}

}
