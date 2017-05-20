package chipmunk.compiler;

public class LabelTarget {
	
	private String name;
	private int codeIndex;

	public LabelTarget(String name, int codeIndex){
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
		
		if(other instanceof LabelTarget){
			LabelTarget otherLabel = (LabelTarget) other;
			
			if(name.equals(otherLabel.name)){
				return true;
			}
		}
		
		return false;
	}
}
