package chipmunk;

public class Opcodes {
	
	// Operations
	public static final byte ADD = 0x00;
	public static final byte SUB = 0x01;
	public static final byte MUL = 0x02;
	public static final byte DIV = 0x03;
	public static final byte FDIV = 0x04;
	public static final byte MOD = 0x05;
	public static final byte POW = 0x06;
	public static final byte INC = 0x07;
	public static final byte DEC = 0x08;
	public static final byte POS = 0x09;
	public static final byte NEG = 0x0A;
	public static final byte AND = 0x0B;
	public static final byte OR = 0x0C;
	public static final byte BXOR = 0x0D;
	public static final byte BAND = 0x0E;
	public static final byte BOR = 0x0F;
	public static final byte BNEG = 0x10;
	public static final byte LSHIFT = 0x11;
	public static final byte RSHIFT = 0x12;
	public static final byte URSHIFT = 0x13;
	public static final byte SETATTR = 0x14;
	public static final byte GETATTR = 0x15;
	public static final byte GETAT = 0x16;
	public static final byte SETAT = 0x17;
	public static final byte TRUTH = 0x18;
	public static final byte AS = 0x19;

	// Flow
	public static final byte IF = 0x1A;
	public static final byte CALL = 0x1B;
	public static final byte GOTO = 0x1C;
	public static final byte THROW = 0x1D;
	public static final byte RETURN = 0x1E;
	
	// Stack
	public static final byte POP = 0x1F;
	public static final byte DUP = 0x20;
	public static final byte SWAP = 0x21;
	public static final byte PUSHI = 0x22;
	public static final byte PUSHF = 0x23;
	public static final byte PUSHB = 0x24;
	public static final byte PUSHSTR = 0x25;
	public static final byte PUSHNULL = 0x26;
	
	// Comparison
	public static final byte EQ = 0x27;
	public static final byte GT = 0x28;
	public static final byte LT = 0x29;
	public static final byte GE = 0x2A;
	public static final byte LE = 0x2B;
	public static final byte IS = 0x2C;
	
}