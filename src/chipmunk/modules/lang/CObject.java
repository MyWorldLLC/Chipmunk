package chipmunk.modules.lang;

import chipmunk.Namespace;

public abstract class CObject {
	
	protected Namespace namespace;
	protected CType type;
	
	public CObject(){
		namespace = new Namespace();
	}
	
	public CType getType(){
		return type;
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
}
