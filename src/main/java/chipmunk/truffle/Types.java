package chipmunk.truffle;

import com.oracle.truffle.api.dsl.TypeSystem;

import chipmunk.truffle.runtime.Null;

@TypeSystem({
	boolean.class,
	int.class,
	long.class,
	float.class,
	double.class,
	Null.class,
	String.class,
	Object[].class
})
public class Types {

}
