package chipmunk.compiler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chipmunk.Opcodes;
import chipmunk.modules.reflectiveruntime.CNull;

public class ChipmunkAssembler {
	
	private ByteArrayOutputStream code;
	private int index;
	private int labelNumber;
	
	private List<Object> constantPool;
	
	private List<Label> labels;
	private List<LabelTarget> labelTargets;
	
	public ChipmunkAssembler(){
		this(new ArrayList<Object>());
	}
	
	public ChipmunkAssembler(List<Object> constants){
		code = new ByteArrayOutputStream();
		index = 0;
		labelNumber = 0;
		
		constantPool = constants;
		
		labels = new ArrayList<Label>();
		labelTargets = new ArrayList<LabelTarget>();
	}
	
	public ChipmunkAssembler createMethodAssembler(){
		ChipmunkAssembler assembler = new ChipmunkAssembler(constantPool);
		return assembler;
	}
	
	public List<Object> getConstantPool(){
		return Collections.unmodifiableList(constantPool);
	}
	
	public byte[] getCodeSegment(){
		// resolve labels
		byte[] codeBytes = code.toByteArray();
		for(int i = 0; i < labels.size(); i++){
			Label label = labels.get(i);
			
			boolean resolved = false;
			for(int target = 0; target < labelTargets.size(); target++){
				
				LabelTarget labelTarget = labelTargets.get(target);
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
				// TODO - throw error. Label was not resolved.
				throw new RuntimeException("Unresolved label: " + label.getName());
			}
		}
		return codeBytes;
	}
	/*
	public byte[] makeModuleBinary(){
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		// write magic number
		for(int i = 0; i < BinaryModuleFormat.MAGIC_NUMBER.length; i++){
			os.write(BinaryModuleFormat.MAGIC_NUMBER[i]);
		}
		
		// write constant pool
		os.write(BinaryModuleFormat.CONSTANT_POOL);
		
		for(int i = 0; i < constantPool.size(); i++){
			CObject obj = constantPool.get(i);
			
			if(obj instanceof CBoolean){
				CBoolean cBool = (CBoolean) obj;
				os.write(BinaryModuleFormat.CONSTANT_BOOL);
				
				if(cBool.getValue() == true){
					os.write(1);
				}else{
					os.write(0);
				}
				
			}else if(obj instanceof CInt){
				CInt cInt = (CInt) obj;
				os.write(BinaryModuleFormat.CONSTANT_INT);
				
				int value = cInt.getValue();
				os.write(value >> 24);
				os.write(value >> 16);
				os.write(value >> 8);
				os.write(value);
				
			}else if(obj instanceof CFloat){
				CFloat cFloat = (CFloat) obj;
				os.write(BinaryModuleFormat.CONSTANT_FLOAT);
				
				int value = Float.floatToIntBits(cFloat.getValue());
				os.write(value >> 24);
				os.write(value >> 16);
				os.write(value >> 8);
				os.write(value);
				
			}else if(obj instanceof CString){
				CString cString = (CString) obj;
				os.write(BinaryModuleFormat.CONSTANT_STRING);
				
				byte[] utfBytes = cString.getValue().getBytes(Charset.forName("UTF-8"));
				int length = utfBytes.length;
				
				os.write(length >> 24);
				os.write(length >> 16);
				os.write(length >> 8);
				os.write(length);
				
				for(int index = 0; index < utfBytes.length; index++){
					os.write(utfBytes[index]);
				}
			}else if(obj instanceof CNull){
				os.write(BinaryModuleFormat.CONSTANT_NULL);
			}else if(obj instanceof CCode){
				os.write(BinaryModuleFormat.CONSTANT_CODE);
				
				byte[] codeBytes = ((CCode) obj).getCode();
				int length = codeBytes.length;
				
				os.write(length >> 24);
				os.write(length >> 16);
				os.write(length >> 8);
				os.write(length);
				
				for(int index = 0; index < length; index++){
					os.write(codeBytes[index]);
				}
			}
		}
		
		os.write(BinaryModuleFormat.CONSTANT_POOL);
		
		// write code section
		os.write(BinaryModuleFormat.CODE_SECTION);
		
		byte[] codeBytes = getCodeSegment();
		int codeLength = codeBytes.length;
		
		os.write(codeLength >> 24);
		os.write(codeLength >> 16);
		os.write(codeLength >> 8);
		os.write(codeLength);
		
		for(int i = 0; i < codeBytes.length; i++){
			os.write(codeBytes[i]);
		}
		
		os.write(BinaryModuleFormat.CODE_SECTION);
		
		return os.toByteArray();
	}*/
	
	public void add(){
		code.write(Opcodes.ADD);
		index++;
	}
	
	public void sub(){
		code.write(Opcodes.SUB);
		index++;
	}
	
	public void mul(){
		code.write(Opcodes.MUL);
		index++;
	}
	
	public void div(){
		code.write(Opcodes.DIV);
		index++;
	}
	
	public void fdiv(){
		code.write(Opcodes.FDIV);
		index++;
	}
	
	public void mod(){
		code.write(Opcodes.MOD);
		index++;
	}
	
	public void pow(){
		code.write(Opcodes.POW);
		index++;
	}
	
	public void inc(){
		code.write(Opcodes.INC);
		index++;
	}
	
	public void dec(){
		code.write(Opcodes.DEC);
		index++;
	}
	
	public void pos(){
		code.write(Opcodes.POS);
		index++;
	}
	
	public void neg(){
		code.write(Opcodes.NEG);
		index++;
	}
	
	public void and(){
		code.write(Opcodes.AND);
		index++;
	}
	
	public void or(){
		code.write(Opcodes.OR);
		index++;
	}
	
	public void not(){
		code.write(Opcodes.NOT);
		index++;
	}
	
	public void bxor(){
		code.write(Opcodes.BXOR);
		index++;
	}
	
	public void band(){
		code.write(Opcodes.BAND);
		index++;
	}
	
	public void bor(){
		code.write(Opcodes.BOR);
		index++;
	}
	
	public void bneg(){
		code.write(Opcodes.BNEG);
		index++;
	}
	
	public void lshift(){
		code.write(Opcodes.LSHIFT);
		index++;
	}
	
	public void rshift(){
		code.write(Opcodes.RSHIFT);
		index++;
	}
	
	public void urshift(){
		code.write(Opcodes.URSHIFT);
		index++;
	}
	
	public void _instanceof(){
		code.write(Opcodes.INSTANCEOF);
		index++;
	}
	
	public void setattr(){
		code.write(Opcodes.SETATTR);
		index++;
	}
	
	public void getattr(){
		code.write(Opcodes.GETATTR);
		index++;
	}
	
	public void getat(){
		code.write(Opcodes.GETAT);
		index++;
	}
	
	public void setat(){
		code.write(Opcodes.SETAT);
		index++;
	}
	
	public void getLocal(int localIndex){
		code.write(Opcodes.GETLOCAL);
		code.write(localIndex);
		index += 2;
	}
	
	public void setLocal(int localIndex){
		code.write(Opcodes.SETLOCAL);
		code.write(localIndex);
		index += 2;
	}
	
	public void truth(){
		code.write(Opcodes.TRUTH);
		index++;
	}
	
	public void as(){
		code.write(Opcodes.AS);
		index++;
	}
	
	/*public void _if(Label elseLabel){
		_if(elseLabel.getName());
	}*/
	
	public void _if(String elseLabel){
		code.write(Opcodes.IF);
		index++;
		
		label(elseLabel);
		
		code.write(0);
		code.write(0);
		code.write(0);
		code.write(0);
		
		index += 4;
	}
	
	public void call(byte paramCount){
		code.write(Opcodes.CALL);
		code.write(paramCount);
		index += 2;
	}
	
	public void callAt(byte paramCount){
		code.write(Opcodes.CALLAT);
		code.write(paramCount);
		index += 2;
	}
	
	public Label label(String labelName){
		Label label = new Label(labelName, index);
		labels.add(label);
		return label;
	}
	
	public Label label(){
		
		Label label = new Label(nextLabelName(), index);
		labels.add(label);
		
		return label;
	}
	
	public String nextLabelName(){
		String name = Integer.toString(labelNumber);
		labelNumber++;
		return name;
	}
	
	public LabelTarget setLabelTarget(String label){
		LabelTarget target = new LabelTarget(label, index);
		labelTargets.add(target);
		return target;
	}
	
	public LabelTarget setLabelTarget(Label label){
		LabelTarget target = new LabelTarget(label.getName(), index);
		labelTargets.add(target);
		return target;
	}
	
	public void _goto(Label label){
		_goto(label.getName());
	}
	
	public void _goto(String label){
		code.write(Opcodes.GOTO);
		index++;
		label(label);
		
		code.write(0);
		code.write(0);
		code.write(0);
		code.write(0);
		
		index += 4;
	}
	
	public void _throw(){
		code.write(Opcodes.THROW);
		index++;
	}
	
	public void _return(){
		code.write(Opcodes.RETURN);
		index++;
	}
	
	public void pop(){
		code.write(Opcodes.POP);
		index++;
	}
	
	public void dup(int index){
		code.write(Opcodes.DUP);
		code.write(index >> 24);
		code.write(index >> 16);
		code.write(index >> 8);
		code.write(index);
		index += 5;
	}
	
	public void swap(int index1, int index2){
		code.write(Opcodes.SWAP);
		code.write(index1 >> 24);
		code.write(index1 >> 16);
		code.write(index1 >> 8);
		code.write(index1);
		code.write(index2 >> 24);
		code.write(index2 >> 16);
		code.write(index2 >> 8);
		code.write(index2);
		index += 9;
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
		
		code.write(Opcodes.PUSH);
		code.write(entryIndex >> 24);
		code.write(entryIndex >> 16);
		code.write(entryIndex >> 8);
		code.write(entryIndex);
		index += 5;
	}
	
	public void pushNull(){
		push(new CNull());
	}
	
	public void eq(){
		code.write(Opcodes.EQ);
		index++;
	}
	
	public void gt(){
		code.write(Opcodes.GT);
		index++;
	}
	
	public void lt(){
		code.write(Opcodes.LT);
		index++;
	}
	
	public void ge(){
		code.write(Opcodes.GE);
		index++;
	}
	
	public void le(){
		code.write(Opcodes.LE);
		index++;
	}
	
	public void is(){
		code.write(Opcodes.IS);
		index++;
	}
	
	public void iter(){
		code.write(Opcodes.ITER);
		index++;
	}
	
	public void next(Label label){
		next(label.getName());
	}
	
	public void next(String label){
		code.write(Opcodes.NEXT);
		index++;
		label(label);
		
		code.write(0);
		code.write(0);
		code.write(0);
		code.write(0);
		
		index += 4;
	}
	
	public void range(boolean inclusive){
		code.write(Opcodes.RANGE);
		code.write(inclusive ? 1 : 0);
		index += 2;
	}

}
