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
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.vm.tree.Node;
import chipmunk.vm.tree.nodes.*;

public class ExpressionVisitor implements CodegenVisitor {
	
	protected Codegen codegen;
	protected SymbolTable symbols;
	
	public ExpressionVisitor(Codegen codegen){
		this.codegen = codegen;
		this.symbols = codegen.getActiveSymbols();
	}
	
	public static boolean isExpressionNode(AstNode node) {
		return node.is(NodeType.ID, NodeType.LITERAL, NodeType.LIST,
				NodeType.MAP, NodeType.OPERATOR,
				NodeType.CLASS, NodeType.METHOD);
	}

	@Override
	public Node visit(AstNode node) {

		var symbols = codegen.getActiveSymbols();

		if(node.is(NodeType.ID)){
			//assembler.onLine(node.getLineNumber());
			return codegen.emitLocalAccess(node.getToken().text());
		}else if(node.is(NodeType.BINDING)){
			//assembler.onLine(node.getLineNumber());
			codegen.emitBindingAccess(node.getToken().text());
			// TODO
		}else if(node.is(NodeType.LITERAL)){
			//assembler.onLine(node.getLineNumber());
			switch (node.getToken().type()) {
				case BOOLLITERAL:
					return new Value(Boolean.parseBoolean(node.getToken().text()));
				case INTLITERAL:
					return Value.number(Integer.parseInt(node.getToken().text().replace("_", ""), 10));
				case HEXLITERAL:
					return Value.number(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 16));
				case OCTLITERAL:
					return Value.number(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 8));
				case BINARYLITERAL:
					return Value.number(Integer.parseInt(node.getToken().text().replace("_", "").substring(2), 2));
				case FLOATLITERAL:
					return Value.number(Float.parseFloat(node.getToken().text()));
				case STRINGLITERAL:
					// strip quotes
					String value = node.getToken().text().substring(1, node.getToken().text().length() - 1);
					return new Value(ChipmunkLexer.unescapeString(value));
				case NULL:
					return new Value(null);
				
				default:
					//return;
			}
		}else if(node.is(NodeType.LIST)){
			//assembler.onLine(node.getLineNumber());
			var elements = node.getChildren().stream()
					.map(this::visit)
					.toArray(Node[]::new);

			return new ListNode(elements);
		}else if(node.is(NodeType.MAP)){

			//assembler.onLine(node.getLineNumber());
			//assembler.map(node.childCount());

			var elements = node.getChildren().stream()
					.flatMap(pair -> pair.getChildren().stream())
					.map(this::visit)
					.toArray(Node[]::new);

			return new MapNode(elements);

			/*for(int i = 0; i < node.childCount(); i++){
				//assembler.dup();
				// visit key & value expressions
				AstNode keyValue = node.getChild(i);
				// key
				this.visit(keyValue.getChild(0));
				// value
				this.visit(keyValue.getChild(1));
				//assembler.callAt("setAt", (byte)2);
				//assembler.pop();
			}*/
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
				//assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					return buildOpNode("pos", lhs, null);
				} else {
					return buildOpNode("plus", lhs, rhs);
				}
			}
			case DOUBLEPLUS -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("inc", lhs, null);
			}
			case MINUS -> {
				//assembler.onLine(node.getLineNumber());
				if (rhs == null) {
					return buildOpNode("neg", lhs, null);
				} else {
					return buildOpNode("minus", lhs, rhs);
				}
			}
			case DOUBLEMINUS -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("dec", lhs, null);
			}
			case STAR -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("mul", lhs, rhs);
			}
			case DOUBLESTAR -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("pow", lhs, rhs);
			}
			case FSLASH -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("div", lhs, rhs);
			}
			case DOUBLEFSLASH -> {
				// TODO - line numbers
				return buildOpNode("fdiv", lhs, rhs);
			}
			case PERCENT -> {
				//assembler.onLine(node.getLineNumber());
				return buildOpNode("mod", lhs, rhs);
			}
			case DOUBLEDOTLESS -> {
				//assembler.onLine(node.getLineNumber());
				return new Range(visit(lhs), visit(rhs), false);
			}
			case DOUBLEDOT -> {
				//assembler.onLine(node.getLineNumber());
				return new Range(visit(lhs), visit(rhs), true);
			}
			case BAR -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.bor();
			}
			case DOUBLEBAR -> {
				//assembler.onLine(node.getLineNumber());
				return new Or(visit(lhs), visit(rhs));
			}
			case DOUBLECOLON -> {
				//lhs.visit(this);
				if(rhs.is(NodeType.ID)){
					//assembler.onLine(node.getLineNumber());
					//assembler.bind(rhs.getToken().text());
				}else{
					throw new SyntaxError("Binding node operator requires a compile-time static method name");
				}

			}
			case EXCLAMATION -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.not();
			}
			case TILDE -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.bneg();
			}
			case CARET -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.bxor();
			}
			case DOUBLELESSTHAN -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.lshift();
			}
			case LESSTHAN -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.lt();
				return new Compare(visit(lhs), visit(rhs), Compare.Comparison.LT);
			}
			case TRIPLEMORETHAN -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.urshift();
			}
			case DOUBLEMORETHAN -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.rshift();
			}
			case MORETHAN -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.gt();
				return new Compare(visit(lhs), visit(rhs), Compare.Comparison.GT);
			}
			case DOUBLEAMPERSAND -> {
				//assembler.onLine(node.getLineNumber());
				return new And(visit(lhs), visit(rhs));
			}
			case AMPERSAND -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.band();
			}
			case LBRACKET -> {
				//assembler.onLine(node.getLineNumber());
				return new CallAtNode(symbols.getLocalEndIndex(), visit(lhs),"getAt", visit(rhs));
			}
			case LPAREN -> {
				emitCall(node);
			}
			case DOT -> {
				emitDotGet(node);
			}
			case EQUALS -> {
				return emitAssignment(node);
			}
			case DOUBLEEQUAlS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.eq();
				return new Compare(visit(lhs), visit(rhs), Compare.Comparison.EQ);
			}
			case EXCLAMATIONEQUALS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.eq();
				//assembler.not();
			}
			case IS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.is();
			}
			case LESSEQUALS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.le();
				return new Compare(visit(lhs), visit(rhs), Compare.Comparison.LE);
			}
			case MOREEQUALS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.ge();
				return new Compare(visit(lhs), visit(rhs), Compare.Comparison.GE);
			}
			case INSTANCEOF -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler._instanceof();
			}
			case AS -> {
				//node.visitChildren(this);
				//assembler.onLine(node.getLineNumber());
				//assembler.as();
			}
			case IF -> {
				var test = node.getChild(0);
				var ifBranch = node.getChild(1);
				var elseBranch = node.getChild(2);
				//var elseLabel = assembler.nextLabelName();
				//var endLabel = assembler.nextLabelName();

				//assembler.onLine(test.getLineNumber());
				//test.visit(this);

				//assembler._if(elseLabel);
				//assembler.onLine(ifBranch.getLineNumber());
				//ifBranch.visit(this);
				//assembler._goto(endLabel);

				//assembler.setLabelTarget(elseLabel);
				//assembler.onLine(elseBranch.getLineNumber());
				//elseBranch.visit(this);
				//assembler.setLabelTarget(endLabel);
			}
			default ->
				throw new SyntaxError(
						String.format("Unsupported operator %s at %d:%d",
								operator.text(),
								operator.line(),
								operator.column()));
			}
		}
		return null; // TODO
	}
	
	private Node emitAssignment(AstNode op){
		AstNode lhs = op.getLeft();
		if(lhs.is(NodeType.OPERATOR)){
			if(lhs.getToken().type() == TokenType.DOT){
				//assembler.onLine(lhs.getLineNumber());
				//lhs.getLeft().visit(this);
				String attr = lhs.getRight().getToken().text();

				//assembler.onLine(op.getRight().getLineNumber());
				//op.getRight().visit(this);

				//assembler.onLine(lhs.getLineNumber());
				//assembler.setattr(attr);
			}else if(lhs.getToken().type() == TokenType.LBRACKET){
				//lhs.getLeft().visit(this);
				//lhs.getRight().visit(this);
				//op.getRight().visit(this);
				//assembler.onLine(lhs.getLineNumber());
				//assembler.setat();
				return new CallAtNode(symbols.getLocalEndIndex(), visit(lhs.getLeft()),"setAt", visit(lhs.getRight()), visit(op.getRight()));
			}else{
				// error!
				throw new CompileChipmunk(String.format("Invalid assignment at %d. The left hand side of an assignment"
						+ "must be either an attribute, index, or a local variable.",
						lhs.getToken().line()));
			}
		}else if(lhs.is(NodeType.ID)){
			//assembler.onLine(lhs.getLineNumber());
			//var value = op.getRight().visit(this);
			var assign = codegen.emitLocalAssignment(lhs.getToken().text());
			assign.value = this.visit(op.getRight());
		}
		return null;
	}
	
	private void emitCall(AstNode op){
		if(op.getLeft().is(NodeType.OPERATOR)
				&& op.getLeft().getToken().type() == TokenType.DOT
				&& op.getLeft().getRight().is(NodeType.ID)){
			
			AstNode dotOp = op.getLeft();
			// this is a dot access, so issue a callAt opcode
			AstNode callID = dotOp.getRight();

			//dotOp.getLeft().visit(this);
			//op.visitChildren(this, 1);
			
			int argCount = op.childCount() - 1;
			//assembler.onLine(op.getLineNumber());
			//assembler.callAt(callID.getToken().text(), (byte)argCount);
			
		}else{
			int argCount = op.childCount() - 1;
			//op.visitChildren(this);
			//assembler.onLine(op.getLineNumber());
			//assembler.call((byte) argCount);
		}
	}
	
	private void emitDotGet(AstNode op){

		//assembler.onLine(op.getLeft().getLineNumber());
		//op.getLeft().visit(this);
		//assembler.onLine(op.getLineNumber());

		String attr = op.getRight().getToken().text();
		//assembler.getattr(attr);
	}

	private CallAtNode buildOpNode(String op, AstNode lhs, AstNode rhs){
		var l = visit(lhs);
		if(rhs != null){
			var r = visit(rhs);
			return CallAtNode.operator(symbols.getLocalEndIndex(), l, op, r);
		}
		return CallAtNode.operator(symbols.getLocalEndIndex(), l, op);
	}
}
