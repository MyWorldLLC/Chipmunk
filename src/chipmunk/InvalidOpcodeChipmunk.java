package chipmunk;

public class InvalidOpcodeChipmunk extends AngryChipmunk {

	protected byte opcode;
	
	public InvalidOpcodeChipmunk(byte op){
		super();
		opcode = op;
	}
	
	public InvalidOpcodeChipmunk(byte op, String msg){
		super(msg);
		opcode = op;
	}
	
	public byte getInvalidOpcode(){
		return opcode;
	}
}
