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

package chipmunk.modules.runtime;

import chipmunk.ChipmunkVM;
import chipmunk.RuntimeObject;

public class CMethod implements RuntimeObject, CCallable {
	
	protected Object self;
	protected CMethodCode code;
	
	public CMethod(){
		this(new CMethodCode());
	}
	
	public CMethod(CMethodCode code) {
		super();
		self = CNull.instance();
		this.code = code;
	}
	
	public void setCode(CMethodCode code) {
		this.code = code;
	}
	
	public CMethodCode getCode() {
		return code;
	}
	
	public int getArgCount(){
		return code.argCount;
	}
	
	public void setArgCount(int count){
		code.argCount = count;
	}
	
	public int getDefaultArgCount(){
		return code.defaultArgCount;
	}
	
	public void setDefaultArgCount(int count){
		code.defaultArgCount = count;
	}
	
	public int getLocalCount(){
		return code.localCount;
	}
	
	public void setLocalCount(int count){
		code.localCount = count + 1; // + 1 for self reference
	}
	
	public void setConstantPool(Object[] constantPool){
		code.constantPool = constantPool;
	}
	
	public Object[] getConstantPool(){
		return code.constantPool;
	}
	
	public void setCallSiteCount(int count) {
		code.callSiteCount = count;
	}
	
	public int getCallSiteCount() {
		return code.callSiteCount;
	}
	
	public void setInstructions(byte[] codeSegment) {
		code.instructions = codeSegment;
		code.callCache = new Object[codeSegment.length];
	}
	
	public byte[] getInstructions(){
		return code.instructions;
	}
	
	public Object[] getCallCache(){
		return code.callCache;
	}
	
	public Object getSelf(){
		return self;
	}
	
	public void bind(Object self){
		this.self = self;
	}
	
	public CModule getModule(){
		return code.module;
	}
	
	public void setModule(CModule module){
		this.code.module = module;
	}
	
	public String getDebugSymbol() {
		return code.getDebugSymbol();
	}
	
	@Override
	public Object call(ChipmunkVM vm, Object[] params) {
		//return vm.dispatch(this, params);
		return null;
	}

	public CMethod duplicate(){
		CMethod method = new CMethod();
		method.bind(self);
		method.setCode(code);

		return method;
	}

/*	public CMethod duplicate(ChipmunkVM vm){
		vm.traceMem(16); // references
		
		return this.duplicate();
	}*/
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append(this.getClass().getSimpleName());
		builder.append("[Locals: ");
		builder.append(code.localCount);
		builder.append(", Args: ");
		builder.append(code.argCount);
		builder.append(", Def. Args: ");
		builder.append(code.defaultArgCount);
		builder.append("]");
		
		return builder.toString();
	}
}
