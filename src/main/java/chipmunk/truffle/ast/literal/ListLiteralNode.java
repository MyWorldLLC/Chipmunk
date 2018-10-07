package chipmunk.truffle.ast.literal;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import chipmunk.truffle.ast.ExpressionNode;

public class ListLiteralNode extends ExpressionNode {

	@Child
	private ExpressionNode[] contents;
	
	public ListLiteralNode(ExpressionNode[] contents) {
		this.contents = contents;
	}
	
	@ExplodeLoop
	@Override
	public Object executeGeneric(VirtualFrame frame) {
		
		List<Object> value = new ArrayList<Object>(contents.length);
		CompilerAsserts.compilationConstant(contents.length);
		
		for(int i = 0; i < contents.length; i++) {
			value.add(contents[i].executeGeneric(frame));
		}
		return value;
	}

}
