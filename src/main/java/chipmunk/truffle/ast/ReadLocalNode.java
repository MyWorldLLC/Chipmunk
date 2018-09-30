package chipmunk.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeField(name="slot", type=FrameSlot.class)
public abstract class ReadLocalNode extends ExpressionNode {

	protected abstract FrameSlot getSlot();
	
	@Specialization(guards="isInteger(frame)")
	protected int readInt(VirtualFrame frame) {
		return FrameUtil.getIntSafe(frame, getSlot());
	}
	
	@Specialization(replaces={"readInt"})
	protected Object readObject(VirtualFrame frame) {
		
		if(!frame.isObject(getSlot())) {
			CompilerDirectives.transferToInterpreter();
			
			Object result = frame.getValue(getSlot());
			frame.setObject(getSlot(), result);
			
			return result;
		}
		
		return FrameUtil.getObjectSafe(frame, getSlot());
	}
	
	protected boolean isInteger(VirtualFrame frame) {
		final FrameSlotKind kind = frame.getFrameDescriptor().getFrameSlotKind(getSlot());
		return kind == FrameSlotKind.Int;
	}
}
