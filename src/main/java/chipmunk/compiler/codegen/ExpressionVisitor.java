package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
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
			// TODO - get symbol table mappings
			return false;
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			switch(literal.getType()){
			case BOOLLITERAL:
				// TODO - handle invalid booleans
				assembler.push(new CBoolean(Boolean.parseBoolean(literal.getText())));
				return false;
			case INTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 10)));
				return false;
			case HEXLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 16)));
				return false;
			case OCTLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 8)));
				return false;
			case BINARYLITERAL:
				assembler.push(new CInt(Integer.parseInt(literal.getText(), 2)));
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
			case MINUS:
				if(rhs == null){
					assembler.neg();
				}else{
					assembler.sub();
				}
				return;
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
			default:
				// TODO - extend to include all operators
				return;
			}
		}
	}

}
