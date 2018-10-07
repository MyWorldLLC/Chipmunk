package chipmunk.truffle.ast.literal;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

import chipmunk.truffle.ast.ExpressionNode;

public class MapLiteralNode extends ExpressionNode {

	@Child
	private ExpressionNode[] keys;
	
	@Child
	private ExpressionNode[] values;
	
	public MapLiteralNode(ExpressionNode[] keys, ExpressionNode[] values) {
		this.keys = keys;
		this.values = values;
	}
	
	@ExplodeLoop
	@Override
	public Object executeGeneric(VirtualFrame frame) {
		
		Map<Object, Object> value = new HashMap<Object, Object>(keys.length);
		CompilerAsserts.compilationConstant(keys.length);
		
		for(int i = 0; i < keys.length; i++) {
			value.put(keys[i].executeGeneric(frame), values[i].executeGeneric(frame));
		}
		return value;
	}

}
