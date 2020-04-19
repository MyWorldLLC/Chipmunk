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

import chipmunk.DebugEntry;
import chipmunk.ExceptionBlock;

public class CMethodCode {
	protected int argCount;
	protected int defaultArgCount;
	protected int localCount;
	
	protected byte[] instructions;
	protected Object[] constantPool;
	protected ExceptionBlock[] exceptionTable;
	protected DebugEntry[] debugTable;
	protected String debugSymbol;
	
	protected Object[] callCache;
	
	protected CModule module;
	
	protected int callSiteCount;
	
	public CMethodCode(){
		localCount = 1;
	}
	
	public int getArgCount(){
		return argCount;
	}
	
	public void setArgCount(int count){
		argCount = count;
	}
	
	public int getDefaultArgCount(){
		return defaultArgCount;
	}
	
	public void setDefaultArgCount(int count){
		defaultArgCount = count;
	}
	
	public int getLocalCount(){
		return localCount;
	}
	
	public void setLocalCount(int count){
		localCount = count + 1; // + 1 for self reference
	}
	
	public void setConstantPool(Object[] constantPool){
		this.constantPool = constantPool;
	}
	
	public Object[] getConstantPool(){
		return constantPool;
	}
	
	public void setExceptionTable(ExceptionBlock[] table) {
		exceptionTable = table;
	}
	
	public ExceptionBlock[] getExceptionTable() {
		return exceptionTable;
	}
	
	public void setDebugTable(DebugEntry[] table) {
		debugTable = table;
	}
	
	public DebugEntry[] getDebugTable() {
		return debugTable;
	}
	
	public String getDebugSymbol() {
		return debugSymbol;
	}
	
	public void setDebugSymbol(String symbol) {
		debugSymbol = symbol;
	}
	
	public void setCallSiteCount(int count) {
		callSiteCount = count;
	}
	
	public int getCallSiteCount() {
		return callSiteCount;
	}
	
	public void setCode(byte[] codeSegment) {
		instructions = codeSegment;
		callCache = new Object[codeSegment.length];
	}
	
	public byte[] getCode(){
		return instructions;
	}
	
	public Object[] getCallCache(){
		return callCache;
	}
	
	public CModule getModule(){
		return module;
	}
	
	public void setModule(CModule module){
		this.module = module;
	}
	
}
