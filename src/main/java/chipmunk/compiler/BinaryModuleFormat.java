package chipmunk.compiler;

public class BinaryModuleFormat {
	
	/**
	 * Eight-byte sequence of the hexadecimal ASCII codes that spell out the letters CHIPMUNK.
	 * C - 0x43, H - 0x48, I - 0x49, P - 0x50, M - 0x4D, U - 0x55, N - 0x4E, K - 0x4B
	 */
	public static final byte[] MAGIC_NUMBER = {0x43, 0x48, 0x49, 0x50, 0x4D, 0x55, 0x4E, 0x4B};
	
	public static final byte CONSTANT_POOL = (byte) 0xFE;
	public static final byte CONSTANT_BOOL = (byte) 0x00;
	public static final byte CONSTANT_INT = (byte) 0x01;
	public static final byte CONSTANT_FLOAT = (byte) 0x02;
	public static final byte CONSTANT_STRING = (byte) 0x03;
	public static final byte CONSTANT_NULL = (byte) 0x04;
	public static final byte CONSTANT_CODE = (byte) 0x05;
	
	public static final byte CODE_SECTION = (byte) 0xFF;

}