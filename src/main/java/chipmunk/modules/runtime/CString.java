package chipmunk.modules.runtime;

public class CString {
	
	private String value;
	
	public CString(){
		value = "";
	}
	
	public CString(String value){
		this.value = value;
	}
	
	public String stringValue(){
		return value;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		if(other != null && other instanceof CString){
			if(value.equals(((CString) other).value)){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		return value;
	}

}
