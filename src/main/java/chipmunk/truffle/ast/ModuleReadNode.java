package chipmunk.truffle.ast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeField(name = "variableName", type = String.class)
public class ModuleReadNode extends ExpressionNode {

	protected FrameSlot slot;

	public FrameSlot getSlot() {
		return slot;
	}

	@Specialization(guards = "isInteger(frame)")
	protected int readInt(VirtualFrame frame) {
		return FrameUtil.getIntSafe(frame, getSlot());
	}

	@Specialization(replaces = { "readInt" })
	protected Object readObject(VirtualFrame frame) {

		if (!frame.isObject(getSlot())) {
			CompilerDirectives.transferToInterpreter();

			Object result = frame.getValue(getSlot());
			frame.setObject(getSlot(), result);

			return result;
		}

		return FrameUtil.getObjectSafe(frame, getSlot());
	}

	protected boolean isInteger(VirtualFrame frame) {
		bindSlot(frame);
		final FrameSlotKind kind = frame.getFrameDescriptor().getFrameSlotKind(getSlot());
		return kind == FrameSlotKind.Int;
	}

	protected void bindSlot(VirtualFrame frame) {
		if (slot == null) {
			CompilerDirectives.transferToInterpreterAndInvalidate();
			slot = frame.getFrameDescriptor().findOrAddFrameSlot("self");
		}
	}

	@Override
	public Object executeGeneric(VirtualFrame frame) {
		bindSlot(frame);
		
		try {
			Object self = frame.getObject(slot);
			if(self instanceof Module) {
				// TODO - read from module
			}else {
				// TODO
			}
		} catch (FrameSlotTypeException e) {
			// self not bound to an object
		}
		
		// TODO Auto-generated method stub
		return null;
	}
}
