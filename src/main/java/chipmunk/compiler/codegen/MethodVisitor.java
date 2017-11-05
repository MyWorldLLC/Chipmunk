package chipmunk.compiler.codegen;

import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.modules.lang.CMethod;
import chipmunk.modules.lang.CObject;

public class MethodVisitor implements AstVisitor {

	protected CMethod method;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	protected MethodNode methodNode;
	
	public MethodVisitor(List<CObject> constantPool){
		assembler = new ChipmunkAssembler(constantPool);
	}
	
	@Override
	public boolean preVisit(AstNode node) {
		if(node instanceof OperatorNode){
			OperatorNode op = (OperatorNode) node;
			op.visit(new ExpressionVisitor(assembler, symbols));
			return false;
		}else if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			symbols = methodNode.getSymbolTable();
			// TODO - parameter declarations
			return true;
		}else{
			// statements, blocks, and inner method/class defs
		}
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
