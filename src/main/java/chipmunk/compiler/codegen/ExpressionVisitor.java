/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.compiler.codegen;

import chipmunk.compiler.*;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.ListNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ast.MapNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.modules.runtime.*;

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
			assembler.onLine(node.getLineNumber());
			codegen.emitSymbolAccess(id.getID().getText());
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			assembler.onLine(node.getLineNumber());
			switch (literal.getType()) {
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
				case NULL:
					assembler.push(CNull.instance());
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
			assembler.onLine(node.getLineNumber());
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
			assembler.onLine(node.getLineNumber());
			assembler.map(mapNode.getChildren().size());
		}else if(node instanceof MethodNode){
			MethodVisitor visitor = new MethodVisitor(assembler.getConstantPool(), codegen.getModule());
			visitor.visit(node);
			assembler.onLine(node.getLineNumber());
			assembler.push(visitor.getMethod());
		}
		/*else if(node instanceof ClassNode) {
			ClassVisitor visitor = new ClassVisitor(assembler.getConstantPool(), codegen.getModule());
			visitor.visit(node);
			assembler.push(visitor.getCClass());
		}*/
		else if(node instanceof OperatorNode){
			
			OperatorNode op = (OperatorNode) node;
			Token operator = op.getOperator();
			
			AstNode lhs = op.getLeft();
			AstNode rhs = op.getRight();
			
			// TODO - need to detect unary inc/dec/etc operators that re-assign variables
			// and emit correct code

			switch (operator.getType()) {
			case PLUS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.pos();
				} else {
					assembler.add();
				}
				return;
			case DOUBLEPLUS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.inc();
				return;
			case MINUS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.neg();
				} else {
					assembler.sub();
				}
				return;
			case DOUBLEMINUS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.dec();
			case STAR:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mul();
				return;
			case DOUBLESTAR:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.pow();
				return;
			case FSLASH:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.div();
				return;
			case DOUBLEFSLASH:
				op.visitChildren(this);
				assembler.fdiv();
				return;
			case PERCENT:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mod();
				return;
			case DOUBLEDOTLESS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(false);
				return;
			case DOUBLEDOT:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(true);
				return;
			case BAR:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bor();
				return;
			case DOUBLEBAR:
				emitLogicalOr(op);
				return;
			case EXCLAMATION:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.not();
				return;
			case TILDE:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bneg();
				return;
			case CARET:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bxor();
				return;
			case DOUBLELESSTHAN:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lshift();
				return;
			case LESSTHAN:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lt();
				return;
			case TRIPLEMORETHAN:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.urshift();
				return;
			case DOUBLEMORETHAN:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.rshift();
				return;
			case MORETHAN:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.gt();
				return;
			case DOUBLEAMPERSAND:
				emitLogicalAnd(op);
				return;
			case AMPERSAND:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.band();
				return;
			case LBRACKET:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
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
				assembler.onLine(node.getLineNumber());
				assembler.eq();
				break;
			case EXCLAMATIONEQUALS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.eq();
				assembler.not();
				break;
			case LESSEQUALS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.le();
				break;
			case MOREEQUALS:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.ge();
				break;
			case INSTANCEOF:
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler._instanceof();
				break;
			default:
				throw new SyntaxErrorChipmunk(
						String.format("Unsupported operator %s at %d:%d",
								operator.getText(),
								operator.getLine(),
								operator.getColumn()));
			}
		}

	}
	
	private void emitAssignment(AstNode lhs){
		if(lhs instanceof OperatorNode){
			OperatorNode lOp = (OperatorNode) lhs;
			if(lOp.getOperator().getType() == Token.Type.DOT){
				if(lOp.getRight() instanceof IdNode){
					assembler.onLine(lhs.getLineNumber());
					assembler.push(((IdNode) lOp.getRight()).getID().getText());
				}else{
					lOp.getRight().visit(this);
				}
				lOp.getLeft().visit(this);
				assembler.onLine(lhs.getLineNumber());
				assembler.setattr();
			}else if(lOp.getOperator().getType() == Token.Type.LBRACKET){
				lOp.getRight().visit(this);
				lOp.getLeft().visit(this);
				assembler.onLine(lOp.getLineNumber());
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
			assembler.onLine(lhs.getLineNumber());
			codegen.emitSymbolAssignment(((IdNode) lhs).getID().getText());
		}
	}
	
	private void emitCall(OperatorNode op){
		if(op.getLeft() instanceof OperatorNode 
				&& ((OperatorNode) op.getLeft()).getOperator().getType() == Token.Type.DOT
				&& ((OperatorNode)op.getLeft()).getRight() instanceof IdNode){
			
			OperatorNode dotOp = (OperatorNode) op.getLeft();
			// this is a dot access, so issue a callAt opcode
			IdNode callID = (IdNode) dotOp.getRight();
			
			op.visitChildren(this, 1);
			dotOp.getLeft().visit(this);
			
			int argCount = op.getChildren().size() - 1;
			assembler.onLine(op.getLineNumber());
			assembler.callAt(callID.getID().getText(), (byte)argCount);
			
		}else{
			int argCount = op.getChildren().size() - 1;
			// visit parameters first
			op.visitChildren(this, 1);
			// visit call target, then emit call
			op.getLeft().visit(this);
			assembler.onLine(op.getLineNumber());
			assembler.call((byte) argCount);
		}
	}
	
	private void emitDotGet(OperatorNode op){
		if(op.getRight() instanceof IdNode){
			IdNode attr = (IdNode) op.getRight();
			assembler.onLine(op.getLineNumber());
			assembler.push(attr.getID().getText());
		}else{
			op.getRight().visit(this);
		}
		op.getLeft().visit(this);
		assembler.onLine(op.getLineNumber());
		assembler.getattr();
	}

	private void emitLogicalOr(OperatorNode op){
		// l | r | v
		// T | T | T
		// T | F | T
		// F | T | T
		// F | F | F
		assembler.onLine(op.getLineNumber());
		String caseTrue = assembler.nextLabelName();
		String end = assembler.nextLabelName();

		op.getLeft().visit(this); // 1
		assembler.not(); // 1
		assembler._if(caseTrue); // 0

		// At this point, lhs had to be false, so test rhs
		op.getRight().visit(this); // 1
		// Since lhs was false, the overall expression value is the
		// truth value of the rhs
		assembler.truth(); // 1
		assembler._goto(end);

		// Expression is true - shortcircuit
		assembler.setLabelTarget(caseTrue);
		assembler.push(new CBoolean(true)); // 1

		assembler.setLabelTarget(end);
	}

	private void emitLogicalAnd(OperatorNode op){
		// l | r | v
		// T | T | T
		// T | F | F
		// F | T | F
		// F | F | F
		assembler.onLine(op.getLineNumber());
		String caseFalse = assembler.nextLabelName();
		String end = assembler.nextLabelName();

		op.getLeft().visit(this); // 1
		assembler._if(caseFalse); // 0

		op.getRight().visit(this); // 1
		assembler._if(caseFalse); // 0

		// Expression is true
		assembler.push(new CBoolean(true)); // 1
		assembler._goto(end);

		// Expression is false
		assembler.setLabelTarget(caseFalse);
		assembler.push(new CBoolean(false)); // 1

		assembler.setLabelTarget(end);
	}
}
