package chipmunk;

public class InvalidOpcodeChipmunk extends AngryChipmunk {
	private static final long serialVersionUID = -8090867885080049997L;
	
	protected byte opcode;
	
	public InvalidOpcodeChipmunk(byte op){
		super(String.format("Invalid Opcode: 0x%H", op));
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
