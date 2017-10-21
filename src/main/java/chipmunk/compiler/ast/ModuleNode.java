package chipmunk.compiler.ast;

import chipmunk.compiler.SymbolTable;

public class ModuleNode extends ScopedNode {

	protected String name;
	
	public ModuleNode(){
		super(SymbolTable.Scope.MODULE);
		name = "";
	}
	
	public ModuleNode(String name){
		super();
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void addImport(ImportNode node){
		addChild(node);
	}
	
	public void addClassDef(ClassNode node){
		addChild(node);
	}
	
	public void addVarDec(VarDecNode node){
		addChild(node);
	}
	
	public void addMethodDef(MethodNode node){
		addChild(node);
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(module ");
		builder.append(name);
		
		for(AstNode child : children){
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}
}
