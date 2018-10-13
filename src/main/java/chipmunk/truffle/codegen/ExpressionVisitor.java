package chipmunk.truffle.codegen;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.ListNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ast.MapNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.truffle.ast.ExpressionNode;
import chipmunk.truffle.ast.literal.BooleanLiteralNode;
import chipmunk.truffle.ast.literal.FloatLiteralNode;
import chipmunk.truffle.ast.literal.IntegerLiteralNode;
import chipmunk.truffle.ast.literal.ListLiteralNode;
import chipmunk.truffle.ast.literal.MapLiteralNode;
import chipmunk.truffle.ast.literal.StringLiteralNode;
import chipmunk.truffle.ast.operators.*;


public class ExpressionVisitor implements TruffleAstVisitor<ExpressionNode> {
	
	protected TruffleCodegen codegen;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	
	public ExpressionVisitor(TruffleCodegen codegen){
		this.codegen = codegen;
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

	@SuppressWarnings("incomplete-switch")
	@Override
	public ExpressionNode visit(AstNode node) {
		if(node instanceof IdNode){
			
			IdNode id = (IdNode) node;
			return codegen.emitSymbolAccess(id.getID().getText());
			
		}else if(node instanceof LiteralNode){
			
			Token literal = ((LiteralNode) node).getLiteral();
			switch(literal.getType()){
			// TODO - double and long literals
			case BOOLLITERAL:
				return new BooleanLiteralNode(Boolean.parseBoolean(literal.getText()));
			case INTLITERAL:
				return new IntegerLiteralNode(Integer.parseInt(literal.getText(), 10));
			case HEXLITERAL:
				return new IntegerLiteralNode(Integer.parseInt(literal.getText().substring(2), 16));
			case OCTLITERAL:
				return new IntegerLiteralNode(Integer.parseInt(literal.getText().substring(2), 8));
			case BINARYLITERAL:
				return new IntegerLiteralNode(Integer.parseInt(literal.getText().substring(2), 2));
			case FLOATLITERAL:
				return new FloatLiteralNode(Integer.parseInt(literal.getText().substring(2), 2));
			case STRINGLITERAL:
				// strip quotes
				String value = literal.getText().substring(1, literal.getText().length() - 1);
				return new StringLiteralNode(ChipmunkLexer.unescapeString(value));
			}
		}else if(node instanceof ListNode){
			ListNode listNode = (ListNode) node;
			
			ExpressionNode[] expressions = new ExpressionNode[listNode.getChildren().size()];
			
			for(int i = 0; i < listNode.getChildren().size(); i++){
				expressions[i] = this.visit(listNode.getChildren().get(i));
				
			}
			return new ListLiteralNode(expressions);
		}else if(node instanceof MapNode){
			MapNode mapNode = (MapNode) node;
			
			ExpressionNode[] keys = new ExpressionNode[mapNode.getChildren().size()];
			ExpressionNode[] values = new ExpressionNode[mapNode.getChildren().size()];
			
			for(int i = 0; i < mapNode.getChildren().size(); i++){
				// visit key & value expressions
				AstNode keyValue = mapNode.getChildren().get(i);
				// key
				keys[i] = this.visit(keyValue.getChildren().get(0));
				// value
				values[i] = this.visit(keyValue.getChildren().get(1));
			}
			
			return new MapLiteralNode(keys, values);
		}else if(node instanceof MethodNode){
			//MethodVisitor visitor = new MethodVisitor(assembler.getConstantPool(), codegen.getModule());
			//visitor.visit(node);
			//assembler.push(visitor.getMethod());
		}else if(node instanceof ClassNode) {
			//ClassVisitor visitor = new ClassVisitor(assembler.getConstantPool(), codegen.getModule());
			//visitor.visit(node);
			//assembler.push(visitor.getCClass());
		}else if(node instanceof OperatorNode){
			
			OperatorNode op = (OperatorNode) node;
			Token operator = op.getOperator();
			
			AstNode lhs = op.getLeft();
			AstNode rhs = op.getRight();

			switch (operator.getType()) {
			case PLUS:
				if (rhs == null) {
					return PosNodeGen.create(this.visit(lhs));
				} else {
					return AddNodeGen.create(this.visit(lhs), this.visit(rhs));
				}
			case DOUBLEPLUS:
				return IncrementNodeGen.create(this.visit(lhs));
			case MINUS:
				if (rhs == null) {
					return NegNodeGen.create(this.visit(lhs));
				} else {
					return SubNodeGen.create(this.visit(lhs), this.visit(rhs));
				}
			case DOUBLEMINUS:
				return DecrementNodeGen.create(this.visit(lhs));
			case STAR:
				return MulNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLESTAR:
				return PowerNodeGen.create(this.visit(lhs), this.visit(rhs));
			case FSLASH:
				return DivNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLEFSLASH:
				return FloorDivNodeGen.create(this.visit(lhs), this.visit(rhs));
			case PERCENT:
				return ModNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLEDOTLESS:
				// TODO
				// op.visitChildren(this);
				// assembler.range(false);
				return null;
			case DOUBLEDOT:
				// TODO
				// op.visitChildren(this);
				// assembler.range(true);
				return null;
			case BAR:
				return BitwiseOrNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLEBAR:
				return new LogicalOrNode(this.visit(lhs), this.visit(rhs));
			case EXCLAMATION:
				return NotNodeGen.create(this.visit(lhs));
			case TILDE:
				return BitwiseNegNodeGen.create(this.visit(lhs));
			case CARET:
				return BitwiseXorNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLELESSTHAN:
				return LeftShiftNodeGen.create(this.visit(lhs), this.visit(rhs));
			case LESSTHAN:
				return LessThanNodeGen.create(this.visit(lhs), this.visit(rhs));
			case TRIPLEMORETHAN:
				return UnsignedRightShiftNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLEMORETHAN:
				return RightShiftNodeGen.create(this.visit(lhs), this.visit(rhs));
			case MORETHAN:
				return LogicalGreaterThanNodeGen.create(this.visit(lhs), this.visit(rhs));
			case DOUBLEAMPERSAND:
				return new LogicalAndNode(this.visit(lhs), this.visit(rhs));
			case AMPERSAND:
				return BitwiseAndNodeGen.create(this.visit(lhs), this.visit(rhs));
			case LBRACKET:
				return GetAtNodeGen.create(this.visit(lhs), this.visit(rhs));
			case LPAREN:
				// TODO
				emitCall(op);
				return null;
			case DOT:
				// TODO
				emitDotGet(op);
				return null;
			case EQUALS:
				emitAssignment(lhs, this.visit(rhs));
				return emitAssignment(lhs, this.visit(rhs));
			case DOUBLEEQUAlS:
				return LogicalEqualityNodeGen.create(this.visit(lhs), this.visit(rhs));
			case EXCLAMATIONEQUALS:
				return NotNodeGen.create(LogicalEqualityNodeGen.create(this.visit(lhs), this.visit(rhs)));
			default:
				return null;
			}
		}
		return null;
	}
	
	private ExpressionNode emitAssignment(AstNode lhs, ExpressionNode rhs){
		if(lhs instanceof OperatorNode){
			// attribute or array assignment
			OperatorNode lOp = (OperatorNode) lhs;
			if(lOp.getOperator().getType() == Token.Type.DOT){
				// attribute assignment
				if(lOp.getRight() instanceof IdNode){
					// ... by name
					// assembler.push(((IdNode) lOp.getRight()).getID().getText());
				}else{
					// ... by generic expression
					//lOp.getRight().visit(this);
				}
				//lOp.getLeft().visit(this);
				assembler.setattr();
			}else if(lOp.getOperator().getType() == Token.Type.LBRACKET){
				// array assignment
				//lOp.getRight().visit(this);
				//lOp.getLeft().visit(this);
				// assembler.setat();
			}else{
				// error!
				// TODO - better messages
				throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
						+ "must be either an attribute, index, or a local variable.", 
						  lOp.getOperator().getLine()));
			}
		}else if(lhs instanceof IdNode){
			codegen.emitSymbolAssignment(((IdNode) lhs).getID().getText(), rhs);
		}
		return null;
	}
	
	private void emitCall(OperatorNode op){
		if(op.getLeft() instanceof OperatorNode 
				&& ((OperatorNode) op.getLeft()).getOperator().getType() == Token.Type.DOT){
			
			OperatorNode dotOp = (OperatorNode) op.getLeft();
			// this is a dot access, so issue a callAt opcode
			IdNode callID = (IdNode) dotOp.getRight();
			
			//op.visitChildren(this, 1);
			//dotOp.getLeft().visit(this);
			
			int argCount = op.getChildren().size() - 1;
			// assembler.callAt(callID.getID().getText(), (byte)argCount);
			
		}else{
			// emit direct call
			int argCount = op.getChildren().size() - 1;
			// visit parameters first
			//op.visitChildren(this, 1);
			// visit call target, then emit call
			//op.getLeft().visit(this);
			// assembler.call((byte) argCount);
		}
	}
	
	private void emitDotGet(OperatorNode op){
		if(op.getRight() instanceof IdNode){
			IdNode attr = (IdNode) op.getRight();
			// assembler.push(attr.getID().getText());
		}else{
			//op.getRight().visit(this);
		}
		//op.getLeft().visit(this);
		// assembler.getattr();
	}
	
	
	
	
	
}
