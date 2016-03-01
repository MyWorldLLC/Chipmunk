package chipmunk;

import java.util.ArrayDeque;

public class AngryChipmunk extends RuntimeException {

	private static final long serialVersionUID = 4997822014942264350L;
	
	protected ArrayDeque<DebugInfo> traceFrames;

	public AngryChipmunk(){
		this("");
	}
	
	public AngryChipmunk(String message){
		super(message);
		traceFrames = new ArrayDeque<DebugInfo>();
	}
	
	public void addTraceFrame(DebugInfo info){
		traceFrames.push(info);
	}
}