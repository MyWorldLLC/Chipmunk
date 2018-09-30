package chipmunk.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

@TypeSystem({
	boolean.class,
	int.class,
	long.class,
	float.class,
	double.class,
	String.class,
	Object[].class
})
public class Types {

}
