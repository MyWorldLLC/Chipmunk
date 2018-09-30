package chipmunk.truffle.ast;

import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild(value="child", type=ExpressionNode.class)
public abstract class UnaryOpNode extends ExpressionNode {}
