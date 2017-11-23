package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
import chipmunk.compiler.UnresolvedSymbolChipmunk;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.modules.lang.CBoolean;
import chipmunk.modules.lang.CFloat;
import chipmunk.modules.lang.CInt;
import chipmunk.modules.lang.CString;

public class ExpressionVisitor implements AstVisitor {
	
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	public ExpressionVisitor(ChipmunkAssembler assembler, SymbolTable symbols){
		this.assembler = assembler;
		this.symbols = symbols;
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof IdNode){
			IdNode id = (IdNode) node;
			Symbol symbol = symbols.getSymbol(id.getID().getText());
			
			if(symbol == null){
				throw new UnresolvedSymbolChipmunk(String.format("Undeclared variable %s at %s: %d", id.getID().getText(), id.getID().getFile(), id.getBeginTokenIndex()), id.getID());
			}
			// TODO - support instance, shared, and module level variables
			assembler.getLocal(symbols.getLocalIndex(symbol));
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			switch(literal.getType()){
			case BOOLLITERAL:
				assembler.push(new CBoolean(Boolean.parseBoolean(literal.getText())));
				return;
			case INTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 10)));
				return;
			case HEXLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 16)));
				return;
			case OCTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 8)));
				return;
			case BINARYLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 2)));
				return;
			case FLOATLITERAL:
				assembler.push(new CFloat(Float.parseFloat(literal.getText())));
				return;
			case STRINGLITERAL:
				assembler.push(new CString(literal.getText()));
				return;
				
				default:
					return;
			}
		}else if(node instanceof OperatorNode){
			
			OperatorNode op = (OperatorNode) node;
			Token operator = op.getOperator();
			AstNode lhs = op.getLeft();
			AstNode rhs = op.getRight();

			switch (operator.getType()) {
			case PLUS:
				op.visitChildren(this);
				if (rhs == null) {
					assembler.pos();
				} else {
					assembler.add();
				}
				return;
			case DOUBLEPLUS:
				op.visitChildren(this);
				assembler.inc();
				return;
			case MINUS:
				op.visitChildren(this);
				if (rhs == null) {
					assembler.neg();
				} else {
					assembler.sub();
				}
				return;
			case DOUBLEMINUS:
				op.visitChildren(this);
				assembler.dec();
			case STAR:
				op.visitChildren(this);
				assembler.mul();
				return;
			case FSLASH:
				op.visitChildren(this);
				assembler.div();
				return;
			case DOUBLEFSLASH:
				op.visitChildren(this);
				assembler.fdiv();
				return;
			case PERCENT:
				op.visitChildren(this);
				assembler.mod();
				return;
			case DOUBLESTAR:
				op.visitChildren(this);
				assembler.pow();
				return;
			case DOUBLEDOTLESS:
				// TODO - ranges
			case DOUBLEDOT:
				return;
			case DOUBLEBAR:
				op.visitChildren(this);
				assembler.or();
				return;
			case BAR:
				op.visitChildren(this);
				assembler.bor();
				return;
			case EXCLAMATION:
				op.visitChildren(this);
				assembler.not();
				return;
			case TILDE:
				op.visitChildren(this);
				assembler.bneg();
				return;
			case CARET:
				op.visitChildren(this);
				assembler.bxor();
				return;
			case DOUBLELESSTHAN:
				op.visitChildren(this);
				assembler.lshift();
				return;
			case LESSTHAN:
				op.visitChildren(this);
				assembler.lt();
				return;
			case TRIPLEMORETHAN:
				op.visitChildren(this);
				assembler.urshift();
				return;
			case DOUBLEMORETHAN:
				op.visitChildren(this);
				assembler.rshift();
				return;
			case MORETHAN:
				op.visitChildren(this);
				assembler.gt();
				return;
			case DOUBLEAMPERSAND:
				op.visitChildren(this);
				assembler.and();
				return;
			case AMPERSAND:
				op.visitChildren(this);
				assembler.band();
				return;
			case LBRACKET:
				op.visitChildren(this);
				assembler.getat();
				return;
			case LPAREN:
				op.visitChildren(this);
				int argCount = 0;
				if (rhs != null) {
					argCount = rhs.getChildren().size() - 1;
				}
				assembler.call((byte) argCount);
				return;
			case DOT:
				op.visitChildren(this);
				assembler.getattr();
				return;
			case EQUALS:
				if(lhs instanceof OperatorNode){
					OperatorNode lOp = (OperatorNode) lhs;
					if(lOp.getOperator().getType() == Token.Type.DOT){
						rhs.visit(this);
						lOp.getRight().visit(this);
						lOp.getLeft().visit(this);
						assembler.setattr();
					}else if(lOp.getOperator().getType() == Token.Type.LBRACKET){
						rhs.visit(this);
						lOp.getRight().visit(this);
						lOp.getLeft().visit(this);
						assembler.setat();
					}else{
						// syntax error!
					}
				}else if(lhs instanceof IdNode){
					rhs.visit(this);
					// TODO - handle non-local scopes
					assembler.setLocal(symbols.getLocalIndex(((IdNode) lhs).getID().getText()));
				}
				return;
			default:
				return;
			}
		}

	}
}
