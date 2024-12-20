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

package chipmunk.compiler.assembler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import chipmunk.binary.DebugEntry;

public class ChipmunkAssembler {
	
	private ByteArrayOutputStream code;
	private int index;
	private List<Object> constantPool;

	private DebugTable debugTable;
	private Labeler labels;
	
	private int callSite;
	
	public ChipmunkAssembler(){
		this(new ArrayList<>());
	}
	
	public ChipmunkAssembler(List<Object> constants){
		code = new ByteArrayOutputStream();
		index = 0;
		
		constantPool = constants;
		
		debugTable = new DebugTable();
		labels = new Labeler();
		
		callSite = 0;
	}
	
	public List<Object> getConstantPool(){
		return constantPool;
	}
	
	public DebugTable getDebugTable(){
		return debugTable;
	}
	
	public byte[] getCodeSegment(){
		// resolve labels
		byte[] codeBytes = code.toByteArray();
		for(int i = 0; i < labels.labelCount(); i++){
			Label label = labels.get(i);
			
			boolean resolved = false;
			for(int target = 0; target < labels.labelTargetCount(); target++){
				
				LabelTarget labelTarget = labels.getTarget(target);
				if(labelTarget.getName().equals(label.getName())){
					
					int targetIndex = labelTarget.getCodeIndex();
					int labelIndex = label.getCodeIndex();
					
					codeBytes[labelIndex] = (byte) (targetIndex >> 24);
					codeBytes[labelIndex + 1] = (byte) (targetIndex >> 16);
					codeBytes[labelIndex + 2] = (byte) (targetIndex >> 8);
					codeBytes[labelIndex + 3] = (byte) targetIndex;
					
					resolved = true;
				}
			}
			
			if(!resolved){
				// Throw error. Label was not resolved.
				throw new IllegalStateException("Unresolved label: " + label.getName());
			}
		}
		return codeBytes;
	}
	
	public int getLabelTarget(String label) {
		for(LabelTarget target : labels.getTargets()) {
			if(target.getName().equals(label)) {
				return target.getCodeIndex();
			}
		}
		return -1;
	}
	
	public int getCallSiteCount() {
		return callSite;
	}
	
	public void add(){
		writeByte(Opcodes.ADD);
	}
	
	public void sub(){
		writeByte(Opcodes.SUB);
	}
	
	public void mul(){
		writeByte(Opcodes.MUL);
	}
	
	public void div(){
		writeByte(Opcodes.DIV);
	}
	
	public void fdiv(){
		writeByte(Opcodes.FDIV);
	}
	
	public void mod(){
		writeByte(Opcodes.MOD);
	}
	
	public void pow(){
		writeByte(Opcodes.POW);
	}
	
	public void inc(){
		writeByte(Opcodes.INC);
	}
	
	public void dec(){
		writeByte(Opcodes.DEC);
	}
	
	public void pos(){
		writeByte(Opcodes.POS);
	}
	
	public void neg(){
		writeByte(Opcodes.NEG);
	}
	
	public void not(){
		writeByte(Opcodes.NOT);
	}
	
	public void bxor(){
		writeByte(Opcodes.BXOR);
	}
	
	public void band(){
		writeByte(Opcodes.BAND);
	}
	
	public void bor(){
		writeByte(Opcodes.BOR);
	}
	
	public void bneg(){
		writeByte(Opcodes.BNEG);
	}
	
	public void lshift(){
		writeByte(Opcodes.LSHIFT);
	}
	
	public void rshift(){
		writeByte(Opcodes.RSHIFT);
	}
	
	public void urshift(){
		writeByte(Opcodes.URSHIFT);
	}
	
	public void _instanceof(){
		writeByte(Opcodes.INSTANCEOF);
	}
	
	public void setattr(String attr){
		writeByte(Opcodes.SETATTR);
		writeInt(getConstantPoolEntry(attr));
	}
	
	public void getattr(String attr){
		writeByte(Opcodes.GETATTR);
		writeInt(getConstantPoolEntry(attr));
	}
	
	public void getat(){
		writeByte(Opcodes.GETAT);
	}
	
	public void setat(){
		writeByte(Opcodes.SETAT);
	}
	
	public void getLocal(int localIndex){
		writeByte(Opcodes.GETLOCAL);
		writeByte(localIndex);
	}
	
	public void setLocal(int localIndex){
		if(localIndex == -1){
			throw new IllegalArgumentException("Invalid local index " + localIndex);
		}
		writeByte(Opcodes.SETLOCAL);
		writeByte(localIndex);
	}

	public void initUpvalue(int localIndex){
		writeByte(Opcodes.INITUPVALUE);
		writeByte(localIndex);
	}

	public void getUpvalue(int localIndex){
		writeByte(Opcodes.GETUPVALUE);
		writeByte(localIndex);
	}

	public void setUpvalue(int localIndex){
		writeByte(Opcodes.SETUPVALUE);
		writeByte(localIndex);
	}
	
	public void truth(){
		writeByte(Opcodes.TRUTH);
	}
	
	public void as(){
		writeByte(Opcodes.AS);
	}

	public void _if(Label elseLabel){
		_if(elseLabel.getName());
	}
	
	public void _if(String elseLabel){
		writeByte(Opcodes.IF);
		
		labels.label(elseLabel, index);
		
		writeInt(0);
		
	}
	
	public void call(byte paramCount){
		writeByte(Opcodes.CALL);
		writeByte(paramCount);
	}
	
	public void callAt(String methodName, byte paramCount){
		writeByte(Opcodes.CALLAT);
		writeByte(paramCount);
		
		int entryIndex = getConstantPoolEntry(methodName);
		
		writeInt(entryIndex);
	}
	
	public void _goto(Label label){
		_goto(label.getName());
	}
	
	public void _goto(String label){
		writeByte(Opcodes.GOTO);
		
		labels.label(label, index);
		
		writeInt(0);
	}

	public void bind(String methodName){
		writeByte(Opcodes.BIND);
		writeInt(getConstantPoolEntry(methodName));
	}
	
	public void _throw(){
		writeByte(Opcodes.THROW);
	}
	
	public void _return(){
		writeByte(Opcodes.RETURN);
	}
	
	public void pop(){
		writeByte(Opcodes.POP);
	}

	public void dup() {
		writeByte(Opcodes.DUP);
	}

	public void swap() {
		writeByte(Opcodes.SWAP);
	}
	
	private int getConstantPoolEntry(Object value){
		int index = constantPool.indexOf(value);
		
		if(index == -1){
			constantPool.add(value);
			index = constantPool.size() - 1;
		}
			
		return index;
	}
	
	public void push(Object value){
		
		int entryIndex = getConstantPoolEntry(value);
		
		writeByte(Opcodes.PUSH);
		
		writeInt(entryIndex);
		
	}
	
	public void pushNull(){
		push(null);
	}
	
	public void eq(){
		writeByte(Opcodes.EQ);
	}
	
	public void gt(){
		writeByte(Opcodes.GT);
	}
	
	public void lt(){
		writeByte(Opcodes.LT);
	}
	
	public void ge(){
		writeByte(Opcodes.GE);
	}
	
	public void le(){
		writeByte(Opcodes.LE);
	}
	
	public void is(){
		writeByte(Opcodes.IS);
	}
	
	public void iter(){
		writeByte(Opcodes.ITER);
	}
	
	public void range(boolean inclusive){
		writeByte(Opcodes.RANGE);
		writeByte(inclusive ? 1 : 0);
	}
	
	public void list(int elementCount){
		writeByte(Opcodes.LIST);
		writeInt(elementCount);
	}
	
	public void map(int elementCount){
		writeByte(Opcodes.MAP);
		writeInt(elementCount);
	}
	
	/*public void init(){
		writeByte(Opcodes.INIT);
	}*/
	
	/*public void getModule(String name){
		writeByte(Opcodes.GETMODULE);
		
		int index = getConstantPoolEntry(name);
		writeInt(index);
	}
	
	public void setModule(String name){
		writeByte(Opcodes.SETMODULE);
		
		int index = getConstantPoolEntry(name);
		
		writeInt(index);
	}*/

	/*public void initModule(int importIndex){
		writeByte(Opcodes.INITMODULE);
		writeInt(importIndex);
	}

	public void _import(int importIndex){
		writeByte(Opcodes.IMPORT);
		writeInt(importIndex);
	}*/

	public void setLabelTarget(String labelName){
		labels.setLabelTarget(labelName, index);
	}

	public String nextLabelName(){
		return labels.nextLabelName();
	}

	public void onLine(int lineNumber){
		debugTable.onLine(lineNumber, index);
	}

	public void closeLine(){
		debugTable.closeLine(index);
	}
	
	private void writeByte(byte b) {
		code.write(b);
		index++;
	}
	
	private void writeByte(int b) {
		writeByte((byte) b);
	}
	
	private void writeShort(int s) {
		//writeShort((short) s);
	}
	
	private void writeShort(short s) {
		code.write(s >> 8);
		code.write(s);
		index += 2;
	}
	
	private void writeInt(int i) {
		code.write(i >> 24);
		code.write(i >> 16);
		code.write(i >> 8);
		code.write(i);
		index += 4;
	}

}
