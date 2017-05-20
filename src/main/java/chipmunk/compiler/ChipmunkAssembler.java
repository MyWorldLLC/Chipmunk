package chipmunk.compiler;

import java.io.ByteArrayOutputStream;

import chipmunk.Opcodes;
import chipmunk.modules.lang.CObject;

public class ChipmunkAssembler {
	
	private ByteArrayOutputStream code;
	private int index;
	
	public ChipmunkAssembler(){
		code = new ByteArrayOutputStream();
		index = 0;
	}
	
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
	
	public void lshift(byte places){
		code.write(Opcodes.LSHIFT);
		code.write(places);
		index += 2;
	}
	
	public void rshift(byte places){
		code.write(Opcodes.RSHIFT);
		code.write(places);
		index += 2;
	}
	
	public void urshift(int places){
		code.write(Opcodes.URSHIFT);
		code.write(places);
		index += 2;
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
	
	public void truth(){
		code.write(Opcodes.TRUTH);
		index++;
	}
	
	public void as(){
		code.write(Opcodes.AS);
		index++;
	}
	
	public void makeclass(){
		code.write(Opcodes.MAKECLASS);
		index++;
	}
	
	public void makemethod(){
		code.write(Opcodes.MAKEMETHOD);
		index++;
	}
	
	public void _if(){
		code.write(Opcodes.IF);
		index++;
	}
	
	public void call(byte paramCount){
		code.write(Opcodes.CALL);
		code.write(paramCount);
		index += 2;
	}
	
	public void _goto(){
		code.write(Opcodes.GOTO);
		index++;
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
	
	public void dup(){
		code.write(Opcodes.DUP);
		index++;
	}
	
	public void swap(){
		code.write(Opcodes.SWAP);
		index++;
	}
	
	public void push(CObject value){
		// TODO
		code.write(Opcodes.PUSH);
		index++;
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

}
