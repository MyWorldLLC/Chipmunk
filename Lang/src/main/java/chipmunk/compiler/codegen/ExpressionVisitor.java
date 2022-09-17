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

import chipmunk.binary.BinaryNamespace;
import chipmunk.compiler.*;
import chipmunk.compiler.assembler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.ListNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ast.MapNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.symbols.SymbolTable;

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
			codegen.emitLocalAccess(id.getID().getText());
		}else if(node instanceof LiteralNode){
			Token literal = ((LiteralNode) node).getLiteral();
			assembler.onLine(node.getLineNumber());
			switch (literal.getType()) {
				case BOOLLITERAL:
					assembler.push(Boolean.parseBoolean(literal.getText()));
					return;
				case INTLITERAL:
					assembler.push(Integer.parseInt(literal.getText().replace("_", ""), 10));
					return;
				case HEXLITERAL:
					assembler.push(Integer.parseInt(literal.getText().replace("_", "").substring(2), 16));
					return;
				case OCTLITERAL:
					assembler.push(Integer.parseInt(literal.getText().replace("_", "").substring(2), 8));
					return;
				case BINARYLITERAL:
					assembler.push(Integer.parseInt(literal.getText().replace("_", "").substring(2), 2));
					return;
				case FLOATLITERAL:
					assembler.push(Float.parseFloat(literal.getText()));
					return;
				case STRINGLITERAL:
					// strip quotes
					String value = literal.getText().substring(1, literal.getText().length() - 1);
					assembler.push(ChipmunkLexer.unescapeString(value));
					return;
				case NULL:
					assembler.push(null);
					return;
				
				default:
					return;
			}
		}else if(node instanceof ListNode){
			ListNode listNode = (ListNode) node;

			assembler.onLine(node.getLineNumber());
			assembler.list(listNode.getChildren().size());

			for(int i = 0; i < listNode.getChildren().size(); i++){
				// visit expression
				assembler.dup();
				this.visit(listNode.getChildren().get(i));
				assembler.callAt("add", (byte)1);
				assembler.pop();
			}

		}else if(node instanceof MapNode){
			MapNode mapNode = (MapNode) node;

			assembler.onLine(node.getLineNumber());
			assembler.map(mapNode.getChildren().size());

			for(int i = 0; i < mapNode.getChildren().size(); i++){
				assembler.dup();
				// visit key & value expressions
				AstNode keyValue = mapNode.getChildren().get(i);
				// key
				this.visit(keyValue.getChildren().get(0));
				// value
				this.visit(keyValue.getChildren().get(1));
				assembler.callAt("put", (byte)2);
				assembler.pop();
			}
		}else if(node instanceof MethodNode){
			MethodVisitor visitor = new MethodVisitor(assembler.getConstantPool(), codegen.getModule());
			visitor.visit(node);

			codegen.getModule().getNamespace().addEntry(
					BinaryNamespace.Entry.makeMethod(visitor.getMethodSymbol().getName(), (byte)0, visitor.getMethod()));

			assembler.onLine(node.getLineNumber());
			// TODO - push closure locals
			// TODO - have to get correct number of method params
			assembler.bind(visitor.getMethodSymbol().getName()); // TODO - have to push closure locals & create a method binding here
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

			switch (operator.getType()) {
			case PLUS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.pos();
				} else {
					assembler.add();
				}
			}
			case DOUBLEPLUS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.inc();
			}
			case MINUS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.neg();
				} else {
					assembler.sub();
				}
			}
			case DOUBLEMINUS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.dec();
			}
			case STAR -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mul();
			}
			case DOUBLESTAR -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.pow();
			}
			case FSLASH -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.div();
			}
			case DOUBLEFSLASH -> {
				op.visitChildren(this);
				assembler.fdiv();
			}
			case PERCENT -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mod();
			}
			case DOUBLEDOTLESS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(false);
			}
			case DOUBLEDOT -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(true);
			}
			case BAR -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bor();
			}
			case DOUBLEBAR -> {
				emitLogicalOr(op);
			}
			case DOUBLECOLON -> {
				lhs.visit(this);
				if(rhs instanceof IdNode idNode){
					assembler.onLine(node.getLineNumber());
					assembler.bind(idNode.getName());
				}else{
					System.out.println(op);
					throw new SyntaxError("Binding operator requires a compile-time static method name");
				}

			}
			case EXCLAMATION -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.not();
			}
			case TILDE -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bneg();
			}
			case CARET -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bxor();
			}
			case DOUBLELESSTHAN -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lshift();
			}
			case LESSTHAN -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lt();
			}
			case TRIPLEMORETHAN -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.urshift();
			}
			case DOUBLEMORETHAN -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.rshift();
			}
			case MORETHAN -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.gt();
			}
			case DOUBLEAMPERSAND -> {
				emitLogicalAnd(op);
			}
			case AMPERSAND -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.band();
			}
			case LBRACKET -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.getat();
			}
			case LPAREN -> {
				emitCall(op);
			}
			case DOT -> {
				emitDotGet(op);
			}
			case EQUALS -> {
				emitAssignment(op);
			}
			case DOUBLEEQUAlS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.eq();
			}
			case EXCLAMATIONEQUALS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.eq();
				assembler.not();
			}
			case IS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.is();
			}
			case LESSEQUALS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.le();
			}
			case MOREEQUALS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.ge();
			}
			case INSTANCEOF -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler._instanceof();
			}
			case AS -> {
				op.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.as();
			}
			default ->
				throw new SyntaxError(
						String.format("Unsupported operator %s at %d:%d",
								operator.getText(),
								operator.getLine(),
								operator.getColumn()));
			}
		}

	}
	
	private void emitAssignment(OperatorNode op){
		AstNode lhs = op.getLeft();
		if(lhs instanceof OperatorNode){
			OperatorNode lOp = (OperatorNode) lhs;
			if(lOp.getOperator().getType() == Token.Type.DOT){
				assembler.onLine(lhs.getLineNumber());
				lOp.getLeft().visit(this);
				String attr = ((IdNode) lOp.getRight()).getID().getText();

				assembler.onLine(op.getRight().getLineNumber());
				op.getRight().visit(this);

				assembler.onLine(lhs.getLineNumber());
				assembler.setattr(attr);
			}else if(lOp.getOperator().getType() == Token.Type.LBRACKET){
				lOp.getLeft().visit(this);
				lOp.getRight().visit(this);
				op.getRight().visit(this);
				assembler.onLine(lOp.getLineNumber());
				assembler.setat();
			}else{
				// error!
				throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
						+ "must be either an attribute, index, or a local variable.", 
						  lOp.getOperator().getLine()));
			}
		}else if(lhs instanceof IdNode){
			assembler.onLine(lhs.getLineNumber());
			op.getRight().visit(this);
			codegen.emitLocalAssignment(((IdNode) lhs).getID().getText());
		}
	}
	
	private void emitCall(OperatorNode op){
		if(op.getLeft() instanceof OperatorNode 
				&& ((OperatorNode) op.getLeft()).getOperator().getType() == Token.Type.DOT
				&& ((OperatorNode)op.getLeft()).getRight() instanceof IdNode){
			
			OperatorNode dotOp = (OperatorNode) op.getLeft();
			// this is a dot access, so issue a callAt opcode
			IdNode callID = (IdNode) dotOp.getRight();

			dotOp.getLeft().visit(this);
			op.visitChildren(this, 1);
			
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

		assembler.onLine(op.getLeft().getLineNumber());
		op.getLeft().visit(this);
		assembler.onLine(op.getLineNumber());

		String attr = ((IdNode) op.getRight()).getID().getText();
		assembler.getattr(attr);
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
		assembler.push(true); // 1

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
		assembler.push(true); // 1
		assembler._goto(end);

		// Expression is false
		assembler.setLabelTarget(caseFalse);
		assembler.push(false); // 1

		assembler.setLabelTarget(end);
	}
}
