package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class ClassNode extends ScopedNode {
	
	protected String name;
	protected String superName;
	
	public ClassNode(){
		super(SymbolTable.Scope.CLASS);
		name = "";
	}
	
	public ClassNode(String name){
		super();
		this.name = name;
	}
	
	public ClassNode(String name, String superName){
		super();
		this.name = name;
		this.superName = superName;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getSuperName(){
		return superName;
	}

	public void setSuperName(String superName){
		this.superName = superName;
	}

	public void addChild(AstNode child){
		super.addChild(child);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(class ");
		builder.append(name);
		
		if(superName != null){
			builder.append(" extends ");
			builder.append(superName);
		}
		
		for(AstNode child : children){
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(")");
		return builder.toString();
	}
}