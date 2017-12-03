package chipmunk.modules.reflectiveruntime;

import chipmunk.ChipmunkContext;
import chipmunk.modules.reflectiveruntime.UnimplementedOperationChipmunk;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;
import chipmunk.reflectors.ContextOperator;

public class CInteger implements ContextOperator {

	private int value;
	
	public CInteger(int value){
		this.value = value;
	}
	
	public CInteger(){
		value = 0;
	}
	
	public int getValue(){
		return value;
	}
	
	public int intValue(){
		return value;
	}
	
	public float floatValue(){
		return value;
	}
	
	public CInteger plus(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value + other.value);
	}
	
	public CFloat plus(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value + other.floatValue());
	}
	
	public CInteger minus(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value - other.value);
	}
	
	public CFloat minus(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value - other.floatValue());
	}
	
	public CInteger mul(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value * other.value);
	}
	
	public CFloat mul(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value * other.floatValue());
	}
	
	public CFloat div(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CFloat(((float)value) / other.value);
	}
	
	public CFloat div(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value / other.floatValue());
	}
	
	public CInteger fdiv(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value / other.value);
	}
	
	public CInteger fdiv(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CInteger((int) (value / other.floatValue()));
	}
	
	public CInteger mod(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value % other.value);
	}
	
	public CFloat mod(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat(value % other.floatValue());
	}
	
	public CInteger pow(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger((int) Math.pow(value, other.value));
	}
	
	public CFloat pow(ChipmunkContext context, CFloat other){
		context.traceMem(4);
		return new CFloat((float) Math.pow(value, other.floatValue()));
	}
	
	public CInteger inc(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(value + 1);
	}
	
	public CInteger dec(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(value - 1);
	}
	
	public CInteger pos(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(Math.abs(value));
	}
	
	public CInteger neg(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(-value);
	}
	
	public CInteger bxor(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value ^ other.value);
	}
	
	public CInteger band(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value & other.value);
	}
	
	public CInteger bor(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value | other.value);
	}
	
	public CInteger bneg(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(~value);
	}
	
	public CInteger lshift(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value << other.value);
	}
	
	public CInteger rshift(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value >> other.value);
	}
	
	public CInteger urshift(ChipmunkContext context, CInteger other){
		context.traceMem(4);
		return new CInteger(value >>> other.value);
	}
	
	public CBoolean truth(ChipmunkContext context){
		context.traceMem(4);
		return value != 0 ? new CBoolean(true) : new CBoolean(false);
	}
	
	public Object as(ChipmunkContext context, Class<?> otherType){
		if(otherType == CInteger.class){
			context.traceMem(4);
			return new CInteger(value);
		}else if(otherType == CFloat.class){
			context.traceMem(4);
			return new CFloat(value);
		}else if(otherType == CBoolean.class){
			return truth(context);
		}else{
			throw new BadConversionChipmunk(String.format("Cannot convert int to %s", otherType.getClass().getSimpleName()), this, otherType);
		}
	}
	
	public CInteger compare(ChipmunkContext context, CInteger other){
		return new CInteger(Integer.compare(value, other.value));
	}
	
	public CInteger compare(ChipmunkContext context, CFloat other){
		return new CInteger(Float.compare(value, other.floatValue()));
	}
	
	public void pack(ChipmunkContext context, NutPacker packer, OutputCapsule out){
		out.write(value);
	}
	
	public void unpack(ChipmunkContext context, NutCracker cracker, InputCapsule in){
		context.traceMem(4);
		value = in.readInt();
	}
	
	public CInteger hash(ChipmunkContext context){
		context.traceMem(4);
		return new CInteger(hashCode());
	}
	
	public int hashCode(){
		return Integer.hashCode(value);
	}
	
	public String string(ChipmunkContext context){
		String stringValue = toString();
		context.traceMem(stringValue.length() * 2);
		return stringValue;
	}
	
	public String toString(){
		return Integer.toString(value);
	}
	
	public CBoolean equals(ChipmunkContext context, CInteger other){
		context.traceMem(1);
		return new CBoolean(value == other.value);
	}
	
	public CBoolean equals(ChipmunkContext context, CFloat other){
		context.traceMem(1);
		return new CBoolean(((float)value) == other.floatValue());
	}
	
	public boolean equals(Object other){
		if(other instanceof CInteger){
			if(((CInteger) other).value == value){
				return true;
			}
		}
		return false;
	}
}
