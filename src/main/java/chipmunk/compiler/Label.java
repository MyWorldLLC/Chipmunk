package chipmunk.compiler;

public class Label {
	
	private String name;
	private int codeIndex;

	public Label(String name, int codeIndex){
		this.name = name;
		this.codeIndex = codeIndex;
	}
	
	public String getName(){
		return name;
	}
	
	public int getCodeIndex(){
		return codeIndex;
	}
	
	public boolean equals(Object other){
		
		if(other instanceof Label){
			Label otherLabel = (Label) other;
			
			if(name.equals(otherLabel.name)){
				return true;
			}
		}
		
		return false;
	}
	
	public String toString(){
		return name + ":" + codeIndex;
	}
}
