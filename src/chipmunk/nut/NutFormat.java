package chipmunk.nut;

public class NutFormat {

	/**
	 * Eight-byte number (long) containing the hexadecimal ASCII codes that spell out the letters CHIPMUNK.
	 * C - 0x43, H - 0x48, I - 0x49, P - 0x50, M - 0x4D, U - 0x55, N - 0x4E, K - 0x4B
	 */
	public static final long MAGIC_NUMBER = 0x434849504D554E4BL;
	public static final byte TABLE_MARKER = (byte) 0xFE;
	public static final byte PRIMARY_INSTANCE = (byte) 0xAA;
	public static final byte SECONDARY_INSTANCE = (byte) 0xBB;
	
}
