package chipmunk.nut;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;

public abstract class Packer {

	protected NutPacker nutPacker;
	
	public Packer(NutPacker packer){
		nutPacker = packer;
	}
	
	protected void registerSecondary(CObject obj){
		nutPacker.registerSecondary(obj);
	}
	
	protected void registerSecondaries(CObject obj){}
	
	protected int getPackIndex(CObject obj){
		return nutPacker.getPackIndex(obj);
	}
	
	protected abstract byte[] pack(CObject obj);
	
	public abstract CType getType();
	
}