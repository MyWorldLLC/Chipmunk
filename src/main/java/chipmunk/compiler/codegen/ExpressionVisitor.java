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
	public boolean preVisit(AstNode node) {
		if(node instanceof IdNode){
			IdNode id = (IdNode) node;
			Symbol symbol = symbols.getSymbol(id.getID().getText());
			
			if(symbol == null){
				throw new UnresolvedSymbolChipmunk(String.format("Undeclared variable %s at %s: %d", id.getID().getText(), id.getID().getFile(), id.getBeginTokenIndex()), id.getID());
			}
			// TODO - support instance, shared, and module level variables
			assembler.getLocal(symbols.getLocalIndex(symbol));
			return false;
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			switch(literal.getType()){
			case BOOLLITERAL:
				assembler.push(new CBoolean(Boolean.parseBoolean(literal.getText())));
				return false;
			case INTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 10)));
				return false;
			case HEXLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 16)));
				return false;
			case OCTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 8)));
				return false;
			case BINARYLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText().substring(2), 2)));
				return false;
			case FLOATLITERAL:
				assembler.push(new CFloat(Float.parseFloat(literal.getText())));
				return false;
			case STRINGLITERAL:
				assembler.push(new CString(literal.getText()));
				return false;
				default:
					return false;
			}
		}else{
			return true;
		}
	}

	@Override
	public void postVisit(AstNode node) {
		if(node instanceof OperatorNode){
			OperatorNode op = (OperatorNode) node;
			Token operator = op.getOperator();
			AstNode rhs = op.getRight();
			
			switch(operator.getType()){
			case PLUS:
				if(rhs == null){
					assembler.pos();
				}else{
					assembler.add();
				}
				return;
			case DOUBLEPLUS:
				assembler.inc();
				return;
			case MINUS:
				if(rhs == null){
					assembler.neg();
				}else{
					assembler.sub();
				}
				return;
			case DOUBLEMINUS:
				assembler.dec();
			case STAR:
				assembler.mul();
				return;
			case FSLASH:
				assembler.div();
				return;
			case DOUBLEFSLASH:
				assembler.fdiv();
				return;
			case PERCENT:
				assembler.mod();
				return;
			case DOUBLESTAR:
				assembler.pow();
				return;
			case DOUBLEDOTLESS:
				// TODO - ranges
			case DOUBLEDOT:
				return;
			case DOUBLEBAR:
				assembler.or();
				return;
			case BAR:
				assembler.bor();
				return;
			case EXCLAMATION:
				assembler.not();
				return;
			case TILDE:
				assembler.bneg();
				return;
			case CARET:
				assembler.bxor();
				return;
			case DOUBLELESSTHAN:
				assembler.lshift();
				return;
			case LESSTHAN:
				assembler.lt();
				return;
			case TRIPLEMORETHAN:
				assembler.urshift();
				return;
			case DOUBLEMORETHAN:
				assembler.rshift();
				return;
			case MORETHAN:
				assembler.gt();
				return;
			case DOUBLEAMPERSAND:
				assembler.and();
				return;
			case AMPERSAND:
				assembler.band();
				return;
			case LBRACKET:
				assembler.getat();
				return;
			case LPAREN:
				int argCount = 0;
				if(rhs != null){
					argCount = rhs.getChildren().size() - 1;
				}
				assembler.call((byte)argCount);
				return;
			case DOT:
				assembler.getattr();
				return;
			default:
				// TODO - assignment
				return;
			}
		}
	}

}
