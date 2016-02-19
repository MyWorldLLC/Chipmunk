package chipmunk.modules.lang;


public class Null extends CObject {
	
	public Null(){
		NullType nullType = new NullType();
		type = nullType;
		nullType.nullObject = this;
		namespace.setVariable("type", nullType);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof Null){
			return true;
		}else{
			return false;
		}
	}
	
}
