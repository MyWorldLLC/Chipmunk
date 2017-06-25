package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ClassBlock extends ParentBlock {
	
	protected String name;
	protected List<String> superNames;
	
	public ClassBlock(Scope parent){
		super(parent);
		superNames = new ArrayList<String>();
	}
	
	public ClassBlock(Scope parent, String name){
		this(parent);
		this.name = name;
	}
	
	public ClassBlock(Scope parent, String name, String... supers){
		this(parent, name);
		
		for(String superName : supers){
			superNames.add(superName);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getSuperNames() {
		return superNames;
	}

	public void addSuperName(String superName) {
		superNames.add(superName);
	}

}
