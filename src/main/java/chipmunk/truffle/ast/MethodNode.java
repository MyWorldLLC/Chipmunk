package chipmunk.truffle.ast;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;

import chipmunk.AngryChipmunk;
import chipmunk.SuspendedChipmunk;
import chipmunk.truffle.ChipmunkLanguage;
import chipmunk.truffle.ast.flow.ReturnException;
import chipmunk.truffle.runtime.Null;

@NodeInfo(language="Chipmunk", shortName="body")
public class MethodNode extends RootNode {
	
	@Children
	private OpNode[] ops;
	
	public MethodNode(ChipmunkLanguage language, FrameDescriptor descriptor) {
		super(language, descriptor);
		Truffle.getRuntime().createCallTarget(this);
	}
	
	public void setOpNodes(OpNode[] ops) {
		this.ops = ops;
	}
	
	@Override
	public Object execute(VirtualFrame frame) {
		
		try {
			OpNode next = ops[0].executeAndGetNext(frame);
			while(next != null) {
				next = next.executeAndGetNext(frame);
			}
		}catch(ReturnException e){
			System.out.println("Returning " + e.getValue());
			return e.getValue();
		} catch (Throwable e) {
			if(e instanceof SuspendedChipmunk) {
				throw (SuspendedChipmunk) e;
			}
			
			throw new AngryChipmunk(e);
		}
		
		return Null.instance();
	}

}
