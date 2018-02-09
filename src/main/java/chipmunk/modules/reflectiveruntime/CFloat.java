package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkVM;
import chipmunk.modules.reflectiveruntime.UnimplementedOperationChipmunk;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;
import chipmunk.reflectors.VMOperator;

public class CFloat implements VMOperator {
	
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
	
	public CFloat plus(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(value + other.floatValue());
	}
	
	public CFloat plus(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value + other.value);
	}
	
	public CFloat minus(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(value - other.floatValue());
	}
	
	public CFloat minus(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value - other.value);
	}
	
	public CFloat mul(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(value * other.floatValue());
	}
	
	public CFloat mul(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value * other.value);
	}
	
	public CFloat div(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(value / other.floatValue());
	}
	
	public CFloat div(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value / other.value);
	}
	
	public CInteger fdiv(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CInteger((int) (value / other.floatValue()));
	}
	
	public CInteger fdiv(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CInteger((int) (value / other.value));
	}
	
	public CFloat mod(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat(value % other.floatValue());
	}
	
	public CFloat mod(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat(value % other.value);
	}
	
	public CFloat pow(ChipmunkVM context, CInteger other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.floatValue()));
	}
	
	public CFloat pow(ChipmunkVM context, CFloat other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.value));
	}
	
	public CFloat inc(ChipmunkVM context){
		context.traceMem(4);
		return new CFloat(value + 1);
	}
	
	public CFloat dec(ChipmunkVM context){
		context.traceMem(4);
		return new CFloat(value - 1);
	}
	
	public CFloat pos(ChipmunkVM context){
		context.traceMem(4);
		return new CFloat(Math.abs(value));
	}
	
	public CFloat neg(ChipmunkVM context){
		context.traceMem(4);
		return new CFloat(-value);
	}
	
	public CBoolean truth(ChipmunkVM context){
		context.traceMem(4);
		return value != 0 ? new CBoolean(true) : new CBoolean(false);
	}
	
	public Object as(ChipmunkVM context, Class<?> otherType){
		if(otherType == CInteger.class){
			context.traceMem(4);
			return new CInteger((int)value);
		}else if(otherType == CFloat.class){
			context.traceMem(4);
			return new CFloat(value);
		}else if(otherType == CBoolean.class){
			return truth(context);
		}else{
			throw new BadConversionChipmunk(String.format("Cannot convert float to %s", otherType.getClass().getSimpleName()), this, otherType);
		}
	}
	
	public CInteger compare(ChipmunkVM context, CInteger other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public CInteger compare(ChipmunkVM context, CFloat other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public CFloatRange range(ChipmunkVM vm, CFloat other, boolean inclusive){
		return new CFloatRange(value, other.value, 1.0f, inclusive);
	}
	
	public CFloatRange range(ChipmunkVM vm, CInteger other, boolean inclusive){
		return new CFloatRange(value, other.getValue(), 1.0f, inclusive);
	}
	
	public void pack(ChipmunkVM context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkVM context, NutCracker cracker, InputCapsule in){
		context.traceMem(4);
		value = in.readFloat();
	}
	
	public CInteger hash(ChipmunkVM context){
		context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Float.hashCode(value);
	}
	
	public String string(ChipmunkVM context){
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