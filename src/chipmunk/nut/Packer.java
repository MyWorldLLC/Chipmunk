package chipmunk.nut;

import java.io.ByteArrayOutputStream;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;

public interface Packer {

	public void pack(CObject obj, ByteArrayOutputStream outStream, NutPacker packer);
	public CType getType();
	
}
