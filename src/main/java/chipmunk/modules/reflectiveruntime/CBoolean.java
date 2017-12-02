package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkContext;
import chipmunk.modules.lang.UnimplementedOperationChipmunk;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;
import chipmunk.reflectors.ContextOperator;

public class CBoolean implements ContextOperator {
	
	private boolean value;
	
	public CBoolean(boolean value){
		this.value = value;
	}
	
	public boolean getValue(){
		return value;
	}
	
	public boolean booleanValue(){
		return value;
	}
	
	public CBoolean truth(ChipmunkContext context){
		return this;
	}
	
	public Object as(ChipmunkContext context, Class<?> otherType){
		if(otherType == CInteger.class){
			context.traceMem(4);
			return new CInteger(value ? 1 : 0);
		}else if(otherType == CFloat.class){
			context.traceMem(4);
			return new CFloat(value ? 1.0f : 0.0f);
		}else if(otherType == CBoolean.class){
			return this;
		}else{
			throw new BadConversionChipmunk(String.format("Cannot convert boolean to %s", otherType.getClass().getSimpleName()), this, otherType);
		}
	}
	
	public CInteger compare(ChipmunkContext context, CBoolean other){
		return new CInteger(Boolean.compare(value, other.value));
	}
	
	public void pack(ChipmunkContext context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkContext context, NutCracker cracker, InputCapsule in){
		context.traceMem(4);
		value = in.readBoolean();
	}
	
	public CInteger hash(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Boolean.hashCode(value);
	}
	
	public String string(ChipmunkContext context){
		String stringValue = toString();
		context.traceMem(stringValue.length() * 2);
		return stringValue;
	}
	
	public String toString(){
		return Boolean.toString(value);
	}
	
	public boolean equals(Object other){
		if(other instanceof CBoolean){
			if(((CBoolean) other).value == value){
				return true;
			}
		}
		return false;
	}

}
