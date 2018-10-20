package chipmunk.compiler.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.ListNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ast.MapNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.modules.runtime.CBoolean;
import chipmunk.modules.runtime.CFloat;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CString;

public class ExpressionVisitor implements AstVisitor {
	
	protected Codegen codegen;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	public ExpressionVisitor(Codegen codegen){
		this.codegen = codegen;
		assembler = codegen.getAssembler();
		this.symbols = codegen.getActiveSymbols();
	}
	
	public static boolean isExpressionNode(AstNode node) {
		return node instanceof IdNode
				|| node instanceof LiteralNode
				|| node instanceof ListNode
				|| node instanceof MapNode
				|| node instanceof MethodNode
				|| node instanceof ClassNode
				|| node instanceof OperatorNode;
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof IdNode){
			IdNode id = (IdNode) node;
			codegen.emitSymbolAccess(id.getID().getText());
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			switch(literal.getType()){
			case BOOLLITERAL:
				assembler.push(new CBoolean(Boolean.parseBoolean(literal.getText())));
				return;
			case INTLITERAL:
				assembler.push(new CInteger(Integer.parseInt(literal.getText(), 10)));
				return;
			case HEXLITERAL:
				assembler.push(new CInteger(Integer.parseInt(literal.getText().substring(2), 16)));
				return;
			case OCTLITERAL:
				assembler.push(new CInteger(Integer.parseInt(literal.getText().substring(2), 8)));
				return;
			case BINARYLITERAL:
				assembler.push(new CInteger(Integer.parseInt(literal.getText().substring(2), 2)));
				return;
			case FLOATLITERAL:
				assembler.push(new CFloat(Float.parseFloat(literal.getText())));
				return;
			case STRINGLITERAL:
				// strip quotes
				String value = literal.getText().substring(1, literal.getText().length() - 1);
				assembler.push(new CString(ChipmunkLexer.unescapeString(value)));
				return;
				
				default:
					return;
			}
		}else if(node instanceof ListNode){
			ListNode listNode = (ListNode) node;
			
			for(int i = 0; i < listNode.getChildren().size(); i++){
				// visit expression
				this.visit(listNode.getChildren().get(i));
			}
			assembler.list(listNode.getChildren().size());
		}else if(node instanceof MapNode){
			MapNode mapNode = (MapNode) node;
			mapNode.visitChildren(this);
			
			for(int i = 0; i < mapNode.getChildren().size(); i++){
				// visit key & value expressions
				AstNode keyValue = mapNode.getChildren().get(i);
				// key
				this.visit(keyValue.getChildren().get(0));
				// value
				this.visit(keyValue.getChildren().get(1));
			}
			assembler.map(mapNode.getChildren().size());
		}else if(node instanceof MethodNode){
			MethodVisitor visitor = new MethodVisitor(assembler.getConstantPool(), codegen.getModule());
			visitor.visit(node);
			assembler.push(visitor.getMethod());
		}else if(node instanceof ClassNode) {
			ClassVisitor visitor = new ClassVisitor(assembler.getConstantPool(), codegen.getModule());
			visitor.visit(node);
			assembler.push(visitor.getCClass());
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
			case DOUBLESTAR:
				op.visitChildren(this);
				assembler.pow();
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
			case DOUBLEDOTLESS:
				op.visitChildren(this);
				assembler.range(false);
				return;
			case DOUBLEDOT:
				op.visitChildren(this);
				assembler.range(true);
				return;
			case BAR:
				op.visitChildren(this);
				assembler.bor();
				return;
			case DOUBLEBAR:
				op.visitChildren(this);
				assembler.or();
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
				emitCall(op);
				return;
			case DOT:
				emitDotGet(op);
				return;
			case EQUALS:
				rhs.visit(this);
				emitAssignment(lhs);
				return;
			case DOUBLEEQUAlS:
				op.visitChildren(this);
				assembler.eq();
				break;
			case EXCLAMATIONEQUALS:
				op.visitChildren(this);
				assembler.eq();
				assembler.not();
				break;
			default:
				return;
			}
		}

	}
	
	private void emitAssignment(AstNode lhs){
		if(lhs instanceof OperatorNode){
			OperatorNode lOp = (OperatorNode) lhs;
			if(lOp.getOperator().getType() == Token.Type.DOT){
				if(lOp.getRight() instanceof IdNode){
					assembler.push(((IdNode) lOp.getRight()).getID().getText());
				}else{
					lOp.getRight().visit(this);
				}
				lOp.getLeft().visit(this);
				assembler.setattr();
			}else if(lOp.getOperator().getType() == Token.Type.LBRACKET){
				lOp.getRight().visit(this);
				lOp.getLeft().visit(this);
				assembler.setat();
			}else{
				// error!
				//throw new CompileChipmunk(String.format("Invalid assignment at %s: %d. The left hand side of an assignment"
				//		+ "must be either an attribute, index, or a local variable.", 
				//		lOp.getOperator().getFile(), lOp.getOperator().getLine()));
				throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
						+ "must be either an attribute, index, or a local variable.", 
						  lOp.getOperator().getLine()));
			}
		}else if(lhs instanceof IdNode){
			codegen.emitSymbolAssignment(((IdNode) lhs).getID().getText());
		}
	}
	
	private void emitCall(OperatorNode op){
		if(op.getLeft() instanceof OperatorNode 
				&& ((OperatorNode) op.getLeft()).getOperator().getType() == Token.Type.DOT){
			
			OperatorNode dotOp = (OperatorNode) op.getLeft();
			// this is a dot access, so issue a callAt opcode
			IdNode callID = (IdNode) dotOp.getRight();
			
			op.visitChildren(this, 1);
			dotOp.getLeft().visit(this);
			
			int argCount = op.getChildren().size() - 1;
			assembler.callAt(callID.getID().getText(), (byte)argCount);
			
		}else{
			int argCount = op.getChildren().size() - 1;
			// visit parameters first
			op.visitChildren(this, 1);
			// visit call target, then emit call
			op.getLeft().visit(this);
			assembler.call((byte) argCount);
		}
	}
	
	private void emitDotGet(OperatorNode op){
		if(op.getRight() instanceof IdNode){
			IdNode attr = (IdNode) op.getRight();
			assembler.push(attr.getID().getText());
		}else{
			op.getRight().visit(this);
		}
		op.getLeft().visit(this);
		assembler.getattr();
	}
}
