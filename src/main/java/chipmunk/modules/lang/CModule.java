package chipmunk.modules.lang;

import java.util.Set;

import chipmunk.Namespace;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public class CModule extends CObject {
	
	protected String name;
	protected Namespace namespace;
	protected CCode code;
	
	
	public CModule(){
		type = new CModuleType();
		namespace = new Namespace();
	}
	
	public CModule(String name){
		this();
		this.setName(name);
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public CObject getAttribute(String name){
		return namespace.getObject(name);
	}
	
	public void setAttribute(String name, CObject obj){
		namespace.setVariable(name, obj);
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
	
	public CObject __getAttr__(CObject name){
		
		if(name instanceof CString){
			
			String attrName = ((CString)name).getValue();
			CObject attr = getAttribute(attrName);
			
			if(attr == null){
				throw new UndefinedAttributeChipmunk("Attribute " + attrName + " undefined for module " + this.name);
			}
			
			return attr;
			
		}else{
			throw new UnimplementedOperationChipmunk("Operation __getAttr__(" + name.getType().getName() + ") undefined for type CModule");
		}
	}
	
	public void __setAttr__(CObject name, CObject value){
		
		if(name instanceof CString){
			String attrName = ((CString)name).getValue();
			setAttribute(attrName, value);
		}else{
			throw new UnimplementedOperationChipmunk("Operation __setAttr__ for type CModule undefined for name type " + name.getType().getName());
		}
		
	}
	
	public void __prePack__(NutPacker packer){
		
		if(packer.isPackingCode()){
			
			for(String attrName : namespace.variableNames()){
				
				CObject obj = namespace.getObject(attrName);
				packer.registerSecondary(obj);
				
			}
		}
		
	}
	
	public void __pack__(NutPacker packer, OutputCapsule out){
		
		out.write(name);
		
		Set<String> varNames = namespace.variableNames();
		out.write(varNames.size());
		
		for(String attrName : varNames){
			
			CObject obj = namespace.getObject(attrName);
			int objIndex = packer.getPackIndex(obj);
			
			out.write(attrName);
			out.write(objIndex);
			
		}
		
	}
	
	public void __unpack__(NutCracker cracker, InputCapsule in){
		
		name = in.readString();
		
		int attrCount = in.readInt();
		
		for(int i = 0; i < attrCount; i++){
			
			String attrName = in.readString();
			int attrIndex = in.readInt();
			CObject attrInstance = cracker.getInstance(attrIndex);
			
			namespace.setVariable(attrName, attrInstance);
			
		}
		
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		
		// Modules are considered equal if their names are equal
		if(other instanceof CModule){
			if(((CModule) other).getName().equals(name)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
		
	}
}
