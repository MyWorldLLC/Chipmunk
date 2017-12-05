package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.VarDecNode;

public class VarDecVisitor implements AstVisitor {

	protected Codegen codegen;
	
	public VarDecVisitor(Codegen codegen){
		this.codegen = codegen;
	}
	
	@Override
	public void visit(AstNode node) {
		VarDecNode dec = (VarDecNode) node;
		
		ChipmunkAssembler assembler = codegen.getAssembler();
		SymbolTable symbols = codegen.getActiveSymbols();
		
		Symbol symbol = new Symbol(dec.getVarName());
		symbols.setSymbol(symbol);
		
		if(dec.getAssignExpr() != null){
			dec.getAssignExpr().visit(new ExpressionVisitor(assembler, symbols));
		}else{
			assembler.pushNull();
		}
		assembler.setLocal(symbols.getLocalIndex(symbol));
		assembler.pop();
	}

}
