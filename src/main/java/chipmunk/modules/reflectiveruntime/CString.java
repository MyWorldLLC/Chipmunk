package chipmunk.modules.reflectiveruntime;

public class CString {
	
	private String value;
	
	public CString(){
		value = "";
	}
	
	public CString(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}

}
