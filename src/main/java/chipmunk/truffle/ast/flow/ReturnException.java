package chipmunk.truffle.ast.flow;

import com.oracle.truffle.api.nodes.ControlFlowException;

public class ReturnException extends ControlFlowException {

	private static final long serialVersionUID = 4025831802757479494L;
	
	private final Object value;
	
	public ReturnException(Object value) {
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

}
