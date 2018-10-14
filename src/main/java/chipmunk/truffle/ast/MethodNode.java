package chipmunk.truffle.ast;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;

import chipmunk.truffle.ChipmunkLanguage;
import chipmunk.truffle.ast.flow.ReturnException;
import chipmunk.truffle.runtime.Null;

@NodeInfo(language="Chipmunk", shortName="body")
public class MethodNode extends RootNode {

	@Child
	private StatementNode bodyNode;
	
	public MethodNode(ChipmunkLanguage language, FrameDescriptor descriptor, StatementNode bodyNode) {
		super(language, descriptor);
		this.bodyNode = bodyNode;
		Truffle.getRuntime().createCallTarget(this);
	}
	
	public MethodNode(StatementNode bodyNode) {
		this(null, null, bodyNode);
	}
	
	@Override
	public Object execute(VirtualFrame frame) {
		frame.getFrameDescriptor().findOrAddFrameSlot(0, FrameSlotKind.Int);
		try {
			bodyNode.executeVoid(frame);
		}catch(ReturnException e){
			return e.getValue();
		}
		
		return Null.instance();
	}

}
