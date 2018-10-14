package chipmunk.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild("valueNode")
@NodeField(name="slotIdentifier", type=Object.class)
public abstract class WriteLocalNode extends ExpressionNode {
	
	protected abstract Object getSlotIdentifier();
	protected FrameSlot slot;
	
	public FrameSlot getSlot() {
		return slot;
	}
	
	@Specialization(guards="isIntegerOrIllegal(frame)")
	protected int writeInt(VirtualFrame frame, int value) {
		
		frame.getFrameDescriptor().setFrameSlotKind(getSlot(), FrameSlotKind.Int);
		frame.setInt(getSlot(), value);
		
		return value;
	}
	
	@Specialization(replaces={"writeInt"})
	protected Object writeObject(VirtualFrame frame, Object value) {
		
		frame.getFrameDescriptor().setFrameSlotKind(getSlot(), FrameSlotKind.Object);
		frame.setObject(getSlot(), value);
		
		return value;
	}
	
	protected boolean isIntegerOrIllegal(VirtualFrame frame) {
		bindSlot(frame);
		final FrameSlotKind kind = frame.getFrameDescriptor().getFrameSlotKind(getSlot());
		return kind == FrameSlotKind.Int || kind == FrameSlotKind.Illegal;
	}
	
	protected void bindSlot(VirtualFrame frame) {
		if(slot == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			slot = frame.getFrameDescriptor().findOrAddFrameSlot(getSlotIdentifier());
		}
	}
}
