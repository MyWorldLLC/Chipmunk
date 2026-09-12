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

import chipmunk.runtime.CMethod;
import chipmunk.vm.hazel.Fiber;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ChipmunkException extends RuntimeException {

	protected final Object payload;
	protected final Fiber fiber;
	protected String[] stackTraceFrames;

	public ChipmunkException(Fiber fiber){
		this(fiber, null);
	}

	public ChipmunkException(Fiber fiber, String message){
		this(fiber, message, null);
	}

	public ChipmunkException(Fiber fiber, String message, Throwable cause){
		super(message, cause);
		this.fiber = fiber;
		fiber.markExceptionTraceTop();
		payload = null;
	}

	public ChipmunkException(){
		fiber = null;
		payload = null;
	}

	public ChipmunkException(Object payload){
		this.payload = payload;
		this.fiber = null;
	}

	public Fiber fiber(){
		return fiber;
	}

	public Object getPayload(){
		return payload;
	}

	public void populateStackTrace(){
		if(stackTraceFrames == null && fiber != null){
			stackTraceFrames = new String[fiber.callStackDepth()];
			for(int stackPtr = 0; stackPtr < fiber.callStackDepth(); stackPtr++){
				var frame = fiber.callFrames[stackPtr];
				var entry = debugEntry(frame.method, frame.ip);

				stackTraceFrames[stackTraceFrames.length - 1 - stackPtr] = frame.method.debugName() +
						"(" + frame.method.module().name() + ":" + (entry != null && entry.line() > 0 ? entry.line() : "<unknown>") + ")";
			}
		}
	}

	protected CMethod.DebugEntry debugEntry(CMethod m, int ip){
		var debugTable = m.debugTable();
		if(debugTable == null){
			return null;
		}
		for(var i = 0; i < debugTable.length; i++){
			var entry = debugTable[i];
			if(entry.beginIp() <= ip && ip < entry.endIp()){
				return entry;
			}
		}
		return null;
	}

	public String formatStackTrace(){
		if(fiber == null){
			return "<Chipmunk trace unavailable>";
		}
		if(stackTraceFrames == null){
			populateStackTrace();
		}
		var message = getMessage();
		StringBuilder formatted = new StringBuilder(getClass().getName() + ": " + (message != null ? message : ""));
		for(var line : stackTraceFrames){
			formatted.append("\n    at ").append(line);
		}
		return formatted.toString();
	}

	@Override
	public void printStackTrace(){
		printStackTrace(System.err);
	}

	@Override
	public void printStackTrace(PrintStream s){
		s.println("<Chipmunk trace>");
		s.println(formatStackTrace());
		s.println();
		s.println("<VM trace>");
		super.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s){
		s.println("<Chipmunk trace>");
		s.println(formatStackTrace());
		s.println();
		s.println("<VM trace>");
		super.printStackTrace(s);
	}

}