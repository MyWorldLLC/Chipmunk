package chipmunk;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class AngryChipmunk extends RuntimeException {

	private static final long serialVersionUID = 4997822014942264350L;

	protected List<CTraceFrame> traceFrames;

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
		traceFrames = new ArrayList<>();
	}

	public void addTraceFrame(CTraceFrame info){
		traceFrames.add(info);
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

		writer.print("Native ");
		super.printStackTrace(writer);
		/*for(StackTraceElement te : super.getStackTrace()) {
			writer.println("    at " + te.toString());
		}*/
		writer.flush();
	}
	
	@Override
	public void printStackTrace(PrintStream os) {
		PrintWriter writer = new PrintWriter(os);
		printStackTrace(writer);
		writer.flush();
	}
	
	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

}