package chipmunk.nut;

public class NutFormat {

	/**
	 * Eight-byte sequence of the hexadecimal ASCII codes that spell out the letters CHIPMUNK.
	 * C - 0x43, H - 0x48, I - 0x49, P - 0x50, M - 0x4D, U - 0x55, N - 0x4E, K - 0x4B
	 */
	public static final byte[] MAGIC_NUMBER = {0x43, 0x48, 0x49, 0x50, 0x4D, 0x55, 0x4E, 0x4B};
	public static final byte TABLE_MARKER = (byte) 0xFE;
	public static final byte PRIMARY_INSTANCE = (byte) 0xAA;
	public static final byte SECONDARY_INSTANCE = (byte) 0xBB;
	
}
