package chipmunk.modules.runtime;

import chipmunk.AngryChipmunk;

public class BadConversionChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 2673936336954560112L;
	
	private final Object obj;
	private final Class<?> targetClass;
	
	public BadConversionChipmunk(){
		this(null, null, null);
	}
	
	public BadConversionChipmunk(String msg){
		this(msg, null, null);
	}
	
	public BadConversionChipmunk(String msg, Object obj, Class<?> targetClass){
		super(msg);
		this.obj = obj;
		this.targetClass = targetClass;
	}
	
	public Object getObject(){
		return obj;
	}
	
	public Class<?> getTargetClass(){
		return targetClass;
	}
}
