package chipmunk.truffle.ast;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;

@NodeChildren({
	@NodeChild(value="left", type=ExpressionNode.class),
	@NodeChild(value="right", type=ExpressionNode.class)
})
public abstract class BinaryOpNode extends ExpressionNode {}
