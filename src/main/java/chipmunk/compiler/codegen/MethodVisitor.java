package chipmunk.compiler.codegen;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.modules.lang.CMethod;

public class MethodVisitor implements AstVisitor {

	CMethod method;
	
	@Override
	public boolean preVisit(AstNode node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postVisit(AstNode node) {
		// TODO Auto-generated method stub

	}
	
	public CMethod getMethod(){
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return null;
	}

}
