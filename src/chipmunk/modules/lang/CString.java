package chipmunk.modules.lang;


public class CString extends CObject {
	
	protected String string;
	
	public CString(){
		super();
		string = "";
	}
	
	public CString(String str){
		super();
		string = str;
	}
	
	public String getValue(){
		return string;
	}
	
	public void setValue(String str){
		string = str;
	}
}
