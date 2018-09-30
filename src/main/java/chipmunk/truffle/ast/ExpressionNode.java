package chipmunk.truffle.ast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;

import chipmunk.truffle.TypesGen;

public abstract class ExpressionNode extends StatementNode {

	public abstract Object executeGeneric(VirtualFrame frame);
	
	@Override
	public void executeVoid(VirtualFrame frame) {
		executeGeneric(frame);
	}
	
	public boolean executeBoolean(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectBoolean(executeGeneric(frame));
	}
	
	public int executeInteger(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectInteger(executeGeneric(frame));
	}
	
	public long executeLong(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectLong(executeGeneric(frame));
	}
	
	public float executeFloat(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectFloat(executeGeneric(frame));
	}
	
	public double executeDouble(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectDouble(executeGeneric(frame));
	}
	
	public String executeString(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectString(executeGeneric(frame));
	}
	
	public Object[] executeObjectArray(VirtualFrame frame) throws UnexpectedResultException {
		return TypesGen.expectObjectArray(executeGeneric(frame));
	}

}
