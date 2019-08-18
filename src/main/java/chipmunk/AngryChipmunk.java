package chipmunk;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;

public class AngryChipmunk extends RuntimeException {

	private static final long serialVersionUID = 4997822014942264350L;

	protected ArrayDeque<CTraceFrame> traceFrames;

	public AngryChipmunk(){
		this(null, null);
	}
	
	public AngryChipmunk(String message){
		this(message, null);
	}

	public AngryChipmunk(Throwable cause){
		this(cause.getMessage(), cause);
	}
	
	public AngryChipmunk(String message, Throwable cause){
		super(message, cause);
		traceFrames = new ArrayDeque<CTraceFrame>();
	}

	public void addTraceFrame(CTraceFrame info){
		traceFrames.push(info);
	}
	
	public CTraceFrame[] getTraceFrames() {
		return traceFrames.toArray(new CTraceFrame[traceFrames.size()]);
	}
	
	@Override
	public void printStackTrace(PrintWriter writer) {
		
		if(super.getMessage() != null) {
			writer.println(super.getMessage());
		}
		
		for(CTraceFrame frame : traceFrames) {
			writer.println("    at " + frame.toString());
		}
		
		for(StackTraceElement te : super.getStackTrace()) {
			writer.println("    at " + te.toString());
		}
	}
	
	@Override
	public void printStackTrace(PrintStream os) {
		printStackTrace(new PrintWriter(os));
	}
	
	@Override
	public void printStackTrace() {
		printStackTrace(System.out);
	}

}