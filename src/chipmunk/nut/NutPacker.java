package chipmunk.nut;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chipmunk.modules.lang.CObject;
import chipmunk.modules.lang.CType;

public class NutPacker {
	
	public static final byte NUT_VERSION = (byte) 0x10;
	
	protected Map<CType, Packer> packers;
	
	protected IdentityHashMap<CObject, Integer> identityMap;
	protected int identityCounter;
	
	protected Set<CType> types;
	
	protected List<CObject> instances;
	protected Deque<Integer> primaries;
	
	private boolean isPackingCode;
	
	protected List<byte[]> buffers;
	
	public NutPacker(){
		
		packers = new HashMap<CType, Packer>();
		
		identityMap = new IdentityHashMap<CObject, Integer>();
		identityCounter = 0;
		
		types = new HashSet<CType>();
		
		instances = new ArrayList<CObject>();
		primaries = new ArrayDeque<Integer>();
		
		isPackingCode = false;
		
		buffers = new ArrayList<byte[]>();
		
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
	
	public boolean isPackingCode(){
		return isPackingCode;
	}
	
	public void packCode(boolean packCode){
		isPackingCode = packCode;
	}
	
	public void reset(){
		
		identityMap.clear();
		identityCounter = 0;
		
		types.clear();
		
		instances.clear();
		primaries.clear();
		
		buffers.clear();
		
	}
	
	private void register(CObject obj, boolean primary){
		
		int identity = identityCounter;
		identityMap.put(obj, identityCounter);
		identityCounter++;
		
		instances.set(identity, obj);
		if(primary){
			primaries.add(identity);
		}
		
		types.add(obj.getType());
		
	}
	
	protected void registerSecondary(CObject obj){
		register(obj, false);
	}
	
	protected int getPackIndex(CObject obj){
		return identityMap.get(obj);
	}
	
	public byte[] pack(Nut nut){
		
		List<CObject> nutInstances = nut.getInstances();
		
		// perform register phase
		for(int i = 0; i < nutInstances.size(); i++){
			
			CObject obj = nutInstances.get(i);
			
			CType objType = obj.getType();
			
			Packer packer = packers.get(objType);
			
			if(packer == null){
				throw new UnknownTypeChipmunk("Could not pack instance of " + objType.getName() + ": No packer registered for type.");
			}
			
			// register the primary object
			register(obj, true);
			// register any secondaries
			packer.registerSecondaries(obj);
		}
		
		// invoke pack phase on packers
		for(int i = 0; i < instances.size(); i++){
			
			CObject obj = instances.get(i);
			
			Packer packer = packers.get(obj.getType());
			
			// no need to check for null packer - that's handled in the register phase, and nothing could have changed
			// which packers are available
			
			buffers.set(i, packer.pack(obj));
		}
		
		// prepare UTF-8 buffers of metadata and type entries
		Map<String, String> metadata = nut.getMetaData();
		
		int metadataCount = metadata.size();
		byte[][] metaKeys = new byte[metadataCount][];
		byte[][] metaValues = new byte[metadataCount][];
		
		int metaIndex = 0;
		for(String key : metadata.keySet()){
			
			byte[] keyBytes = key.getBytes(Charset.forName("UTF-8"));
			byte[] valueBytes = metadata.get(key).getBytes(Charset.forName("UTF-8"));
			
			metaKeys[metaIndex] = keyBytes;
			metaValues[metaIndex] = valueBytes;
			
			metaIndex++;
		}
		
		int typeCount = types.size();
		byte[][] typeNames = new byte[typeCount][];
		byte[][] moduleNames = new byte[typeCount][];
		
		int typeIndex = 0;
		for(CType type : types){
			
			byte[] typeBytes = type.getName().getBytes(Charset.forName("UTF-8"));
			byte[] moduleBytes = type.getModule().getName().getBytes(Charset.forName("UTF-8"));
			
			typeNames[typeIndex] = typeBytes;
			moduleNames[typeIndex] = moduleBytes;
			
			typeIndex++;
		}
		
		// calculate output buffer size and allocate
		int bufferSize = 0;
		
		// each String entry has the length of the string prepended in the file
		bufferSize += 4 * metaKeys.length;
		for(int i = 0; i < metaKeys.length; i++){
			bufferSize += metaKeys[i].length;
		}
		
		bufferSize += 4 * metaValues.length;
		for(int i = 0; i < metaValues.length; i++){
			bufferSize += metaValues[i].length;
		}
		
		bufferSize += 4 * typeNames.length;
		for(int i = 0; i < typeNames.length; i++){
			bufferSize += typeNames[i].length;
		}
		
		bufferSize += 4 * moduleNames.length;
		for(int i = 0; i < moduleNames.length; i++){
			bufferSize += moduleNames[i].length;
		}
		
		// add the table begin/end marker sizes
		// there are three tables - the metadata table, the type table, and the instance table,
		// each with a one-byte begin/end marker
		bufferSize += 6;
		
		// add the instance sizes - each instance entry has a one-byte marker, an integer length, and
		// the instance bytes
		for(int i = 0; i < buffers.size(); i++){
			bufferSize += 5 + buffers.get(i).length;
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
		
		// write magic number and version
		
		buffer.putLong(NutFormat.MAGIC_NUMBER);
		buffer.put(NUT_VERSION);
		
		// write metadata
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		for(int i = 0; i < metadataCount; i++){
			
			byte[] key = metaKeys[i];
			byte[] value = metaValues[i];
			
			buffer.putInt(key.length);
			buffer.put(key);
			
			buffer.putInt(value.length);
			buffer.put(value);
			
		}
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		// write type table
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		for(int i = 0; i < typeCount; i++){
			
			byte[] type = typeNames[i];
			byte[] module = moduleNames[i];
			
			buffer.putInt(type.length);
			buffer.put(type);
			
			buffer.putInt(module.length);
			buffer.put(module);
			
		}
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		// write packed buffers with primary/secondary markers
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		for(int i = 0; i < buffers.size(); i++){
			
			if(i == primaries.peek()){
				buffer.put(NutFormat.PRIMARY_INSTANCE);
				primaries.pop();
			}else{
				buffer.put(NutFormat.SECONDARY_INSTANCE);
			}
			
			byte[] packedBuffer = buffers.get(i);
			
			buffer.putInt(packedBuffer.length);
			buffer.put(packedBuffer);
			
		}
		
		buffer.put(NutFormat.TABLE_MARKER);
		
		return buffer.array();
	}

}
