/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

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