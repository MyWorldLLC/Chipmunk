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
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
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
				|| node.is(NodeType.LITERAL, NodeType.LIST, NodeType.MAP, NodeType.OPERATOR, NodeType.CLASS)
				|| node instanceof MethodNode;
	}

	@Override
	public void visit(AstNode node) {

		if(node.is(NodeType.ID)){
			assembler.onLine(node.getLineNumber());
			codegen.emitLocalAccess(node.getToken().text());
		}else if(node.is(NodeType.LITERAL)){
			assembler.onLine(node.getLineNumber());
			switch (node.getToken().type()) {
				case BOOLLITERAL:
					assembler.push(Boolean.parseBoolean(node.getToken().text()));
					return;
				case INTLITERAL:
					assembler.push(Integer.parseInt(node.getToken().text().replace("_", ""), 10));
					return;
				case HEXLITERAL:
					assembler.push(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 16));
					return;
				case OCTLITERAL:
					assembler.push(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 8));
					return;
				case BINARYLITERAL:
					assembler.push(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 2));
					return;
				case FLOATLITERAL:
					assembler.push(Float.parseFloat(node.getToken().text()));
					return;
				case STRINGLITERAL:
					// strip quotes
					String value = node.getToken().text().substring(1, node.getToken().text().length() - 1);
					assembler.push(ChipmunkLexer.unescapeString(value));
					return;
				case NULL:
					assembler.push(null);
					return;
				
				default:
					return;
			}
		}else if(node.is(NodeType.LIST)){

			assembler.onLine(node.getLineNumber());
			assembler.list(node.getChildren().size());

			for(int i = 0; i < node.getChildren().size(); i++){
				// visit expression
				assembler.dup();
				this.visit(node.getChildren().get(i));
				assembler.callAt("add", (byte)1);
				assembler.pop();
			}

		}else if(node.is(NodeType.MAP)){

			assembler.onLine(node.getLineNumber());
			assembler.map(node.getChildren().size());

			for(int i = 0; i < node.getChildren().size(); i++){
				assembler.dup();
				// visit key & value expressions
				AstNode keyValue = node.getChildren().get(i);
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
		else if(node.is(NodeType.OPERATOR)){

			Token operator = node.getToken();
			
			AstNode lhs = node.getLeft();
			AstNode rhs = node.getRight();

			switch (operator.type()) {
			case PLUS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.pos();
				} else {
					assembler.add();
				}
			}
			case DOUBLEPLUS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.inc();
			}
			case MINUS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					assembler.neg();
				} else {
					assembler.sub();
				}
			}
			case DOUBLEMINUS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.dec();
			}
			case STAR -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mul();
			}
			case DOUBLESTAR -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.pow();
			}
			case FSLASH -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.div();
			}
			case DOUBLEFSLASH -> {
				node.visitChildren(this);
				assembler.fdiv();
			}
			case PERCENT -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.mod();
			}
			case DOUBLEDOTLESS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(false);
			}
			case DOUBLEDOT -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.range(true);
			}
			case BAR -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bor();
			}
			case DOUBLEBAR -> {
				emitLogicalOr(node);
			}
			case DOUBLECOLON -> {
				lhs.visit(this);
				if(rhs instanceof IdNode idNode){
					assembler.onLine(node.getLineNumber());
					assembler.bind(idNode.getName());
				}else{
					throw new SyntaxError("Binding nodeerator requires a compile-time static method name");
				}

			}
			case EXCLAMATION -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.not();
			}
			case TILDE -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bneg();
			}
			case CARET -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.bxor();
			}
			case DOUBLELESSTHAN -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lshift();
			}
			case LESSTHAN -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.lt();
			}
			case TRIPLEMORETHAN -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.urshift();
			}
			case DOUBLEMORETHAN -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.rshift();
			}
			case MORETHAN -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.gt();
			}
			case DOUBLEAMPERSAND -> {
				emitLogicalAnd(node);
			}
			case AMPERSAND -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.band();
			}
			case LBRACKET -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.getat();
			}
			case LPAREN -> {
				emitCall(node);
			}
			case DOT -> {
				emitDotGet(node);
			}
			case EQUALS -> {
				emitAssignment(node);
			}
			case DOUBLEEQUAlS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.eq();
			}
			case EXCLAMATIONEQUALS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.eq();
				assembler.not();
			}
			case IS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.is();
			}
			case LESSEQUALS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.le();
			}
			case MOREEQUALS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.ge();
			}
			case INSTANCEOF -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler._instanceof();
			}
			case AS -> {
				node.visitChildren(this);
				assembler.onLine(node.getLineNumber());
				assembler.as();
			}
			default ->
				throw new SyntaxError(
						String.format("Unsupported operator %s at %d:%d",
								operator.text(),
								operator.line(),
								operator.column()));
			}
		}

	}
	
	private void emitAssignment(AstNode op){
		AstNode lhs = op.getLeft();
		if(lhs.is(NodeType.OPERATOR)){
			if(lhs.getToken().type() == TokenType.DOT){
				assembler.onLine(lhs.getLineNumber());
				lhs.getLeft().visit(this);
				String attr = ((IdNode) lhs.getRight()).getID().text();

				assembler.onLine(op.getRight().getLineNumber());
				op.getRight().visit(this);

				assembler.onLine(lhs.getLineNumber());
				assembler.setattr(attr);
			}else if(lhs.getToken().type() == TokenType.LBRACKET){
				lhs.getLeft().visit(this);
				lhs.getRight().visit(this);
				op.getRight().visit(this);
				assembler.onLine(lhs.getLineNumber());
				assembler.setat();
			}else{
				// error!
				throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
						+ "must be either an attribute, index, or a local variable.",
						lhs.getToken().line()));
			}
		}else if(lhs instanceof IdNode){
			assembler.onLine(lhs.getLineNumber());
			op.getRight().visit(this);
			codegen.emitLocalAssignment(((IdNode) lhs).getID().text());
		}
	}
	
	private void emitCall(AstNode op){
		if(op.getLeft().is(NodeType.OPERATOR)
				&& op.getLeft().getToken().type() == TokenType.DOT
				&& op.getLeft().getRight() instanceof IdNode){
			
			AstNode dotOp = op.getLeft();
			// this is a dot access, so issue a callAt opcode
			IdNode callID = (IdNode) dotOp.getRight();

			dotOp.getLeft().visit(this);
			op.visitChildren(this, 1);
			
			int argCount = op.getChildren().size() - 1;
			assembler.onLine(op.getLineNumber());
			assembler.callAt(callID.getID().text(), (byte)argCount);
			
		}else{
			int argCount = op.getChildren().size() - 1;
			op.visitChildren(this);
			assembler.onLine(op.getLineNumber());
			assembler.call((byte) argCount);
		}
	}
	
	private void emitDotGet(AstNode op){

		assembler.onLine(op.getLeft().getLineNumber());
		op.getLeft().visit(this);
		assembler.onLine(op.getLineNumber());

		String attr = ((IdNode) op.getRight()).getID().text();
		assembler.getattr(attr);
	}

	private void emitLogicalOr(AstNode op){
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

	private void emitLogicalAnd(AstNode op){
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
