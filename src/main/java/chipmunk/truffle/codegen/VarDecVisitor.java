package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.VarDecNode;

public class VarDecVisitor implements AstVisitor {

	protected TruffleCodegen codegen;
	
	public VarDecVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		VarDecNode dec = (VarDecNode) node;
		
		ChipmunkAssembler assembler = null;//codegen.getAssembler();
		Symbol symbol = codegen.getActiveSymbols().getSymbol(dec.getVarName());
		
		if(dec.getAssignExpr() != null){
			dec.getAssignExpr().visit(new ExpressionVisitor(codegen));
		}else{
			assembler.pushNull();
		}
		codegen.emitSymbolAssignment(symbol.getName());
		// codegen.getAssembler().pop();
	}

}
