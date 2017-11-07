package chipmunk.modules.lang;


public class CNull extends CObject {
	
	public CNull(){
		CNullType nullType = new CNullType();
		type = nullType;
		namespace.setVariable("type", nullType);
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof CNull){
			return true;
		}else{
			return false;
		}
	}
	
}
