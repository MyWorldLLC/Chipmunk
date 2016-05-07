package chipmunk;

import java.util.ArrayDeque;

import chipmunk.modules.lang.CObject;

public class AngryChipmunk extends RuntimeException {

	private static final long serialVersionUID = 4997822014942264350L;
	
	protected ArrayDeque<DebugInfo> traceFrames;
	protected CObject payload;

	public AngryChipmunk(){
		this("", null);
	}
	
	public AngryChipmunk(String message){
		this(message, null);
	}
	
	public AngryChipmunk(String message, Throwable cause){
		super(message);
		traceFrames = new ArrayDeque<DebugInfo>();
	}
	
	public void addTraceFrame(DebugInfo info){
		traceFrames.push(info);
	}
	
	public void setPayload(CObject payload){
		this.payload = payload;
	}
	
	public CObject getPayload(){
		return payload;
	}
}