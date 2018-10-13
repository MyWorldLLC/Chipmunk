package chipmunk.truffle.codegen;

import chipmunk.compiler.Symbol;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.literal.NullLiteralNode;

public class VarDecVisitor implements TruffleAstVisitor<ExpressionNode> {

	protected TruffleCodegen codegen;
	
	public VarDecVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public ExpressionNode visit(AstNode node) {
		VarDecNode dec = (VarDecNode) node;
		
		Symbol symbol = codegen.getActiveSymbols().getSymbol(dec.getVarName());
		
		ExpressionNode value = null;
		
		if(dec.getAssignExpr() != null){
			value = new ExpressionVisitor(codegen).visit(dec.getAssignExpr());
		}else{
			value = new NullLiteralNode();
		}
		
		return codegen.emitSymbolAssignment(symbol.getName(), value);
	}

}
