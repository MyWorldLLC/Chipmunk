package chipmunk.compiler.ast;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;

public class ModuleNode extends BlockNode implements SymbolNode {

	protected Symbol symbol;
	
	public ModuleNode(){
		super(SymbolTable.Scope.MODULE);
		symbol = new Symbol();
	}
	
	public ModuleNode(String name){
		this();
		setName(name);
	}
	
	public void setName(String name) {
		symbol.setName(name);
		getSymbolTable().setDebugSymbol(name);
	}
	
	public String getName() {
		return symbol.getName();
	}
	
	public Symbol getSymbol() {
		return symbol;
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
		builder.append(symbol.getName());
		
		for(AstNode child : children){
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}
}
