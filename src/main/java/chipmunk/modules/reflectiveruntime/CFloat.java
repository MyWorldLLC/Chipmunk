package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkContext;
import chipmunk.modules.lang.CBoolean;
import chipmunk.modules.lang.UnimplementedOperationChipmunk;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;
import chipmunk.reflectors.ContextOperator;

public class CFloat implements ContextOperator {
	
	private float value;

	public CFloat(float value){
		this.value = value;
	}
	
	public float getValue(){
		return value;
	}
	
	public int intValue(){
		return (int) value;
	}
	
	public float floatValue(){
		return value;
	}
	
	public CFloat plus(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(value + other.floatValue());
	}
	
	public CFloat plus(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value + other.value);
	}
	
	public CFloat minus(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(value - other.floatValue());
	}
	
	public CFloat minus(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value - other.value);
	}
	
	public CFloat mul(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(value * other.floatValue());
	}
	
	public CFloat mul(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value * other.value);
	}
	
	public CFloat div(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(value / other.floatValue());
	}
	
	public CFloat div(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value / other.value);
	}
	
	public CInteger fdiv(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger((int) (value / other.floatValue()));
	}
	
	public CInteger fdiv(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CInteger((int) (value / other.value));
	}
	
	public CFloat mod(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(value % other.floatValue());
	}
	
	public CFloat mod(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value % other.value);
	}
	
	public CFloat pow(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.floatValue()));
	}
	
	public CFloat pow(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.value));
	}
	
	public CFloat inc(ChipmunkContext context){
		context.traceMem(4);
		return new CFloat(value + 1);
	}
	
	public CFloat dec(ChipmunkContext context){
		context.traceMem(4);
		return new CFloat(value - 1);
	}
	
	public CFloat pos(ChipmunkContext context){
		context.traceMem(4);
		return new CFloat(Math.abs(value));
	}
	
	public CFloat neg(ChipmunkContext context){
		context.traceMem(4);
		return new CFloat(-value);
	}
	
	public CBoolean truth(ChipmunkContext context){
		context.traceMem(4);
		return value != 0 ? new CBoolean(true) : new CBoolean(false);
	}
	
	public Object as(ChipmunkContext context, Class<?> otherType){
		if(otherType == CInteger.class){
			context.traceMem(4);
			return new CInteger((int)value);
		}else if(otherType == CFloat.class){
			context.traceMem(4);
			return new CFloat(value);
		}else if(otherType == CBoolean.class){
			return truth(context);
		}else{
			throw new UnimplementedOperationChipmunk(String.format("Undefined operation: cannot perform int as %s", otherType.getClass().getSimpleName()));
		}
	}
	
	public CInteger compare(ChipmunkContext context, CInteger other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public CInteger compare(ChipmunkContext context, CFloat other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public void pack(ChipmunkContext context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkContext context, NutCracker cracker, InputCapsule in){
		context.traceMem(4);
		value = in.readFloat();
	}
	
	public CInteger hash(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Float.hashCode(value);
	}
	
	public String string(ChipmunkContext context){
		String stringValue = toString();
		context.traceMem(stringValue.length() * 2);
		return stringValue;
	}
	
	public String toString(){
		return Float.toString(value);
	}
	
	public boolean equals(Object other){
		if(other instanceof CFloat){
			if(((CFloat) other).value == value){
				return true;
			}
		}
		return false;
	}
}
