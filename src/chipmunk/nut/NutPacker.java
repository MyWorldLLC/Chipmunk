package chipmunk.nut;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;

public class NutPacker {
	
	protected Map<CType, Packer> packers;
	
	public NutPacker(){
		packers = new HashMap<CType, Packer>();
	}
	
	public void addPacker(CType type, Packer packer){
		
		if(type != null && packer != null){
			packers.put(type, packer);
		}
		
	}
	
	public Packer getPacker(CType type){
		return packers.get(type);
	}
	
	public void removePacker(CType type){
		packers.remove(type);
	}
	
	public void pack(ByteArrayOutputStream outStream, CObject obj){
		
		CType objType = obj.getType();
		
		Packer packer = packers.get(objType);
		
		if(packer == null){
			throw new UnknownTypeException("Could not pack instance of " + objType.getName() + ": No packer registered for type.");
		}
		
		packer.pack(obj, outStream, this);

	}

}
