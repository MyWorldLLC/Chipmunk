package chipmunk.compiler.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.modules.lang.CClassType;

public class ClassVisitor implements AstVisitor {

	protected CClassType classType;
	
	@Override
	public boolean preVisit(AstNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postVisit(AstNode node) {
		// TODO Auto-generated method stub

	}
	
	public CClassType getCClassType(){
		return classType;
	}

}
