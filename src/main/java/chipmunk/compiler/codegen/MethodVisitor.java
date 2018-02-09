package chipmunk.compiler.codegen;

import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.FlowControlNode;
import chipmunk.compiler.ast.ForNode;
import chipmunk.compiler.ast.IfElseNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.compiler.ast.WhileNode;
import chipmunk.modules.reflectiveruntime.CMethod;

public class MethodVisitor implements AstVisitor {

	protected CMethod method;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	protected Codegen codegen;
	protected MethodNode methodNode;
	
	
	public MethodVisitor(List<Object> constantPool){
		assembler = new ChipmunkAssembler(constantPool);
	}
	
	@Override
	public void visit(AstNode node) {
		
		method = new CMethod();
		
		if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			
			method.setArgCount(methodNode.getParamCount());
			
			symbols = methodNode.getSymbolTable();
			
			codegen = new Codegen(assembler, symbols);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);
			
			codegen.setVisitorForNode(OperatorNode.class, expStatVisitor);
			codegen.setVisitorForNode(MethodNode.class, new MethodVisitor(assembler.getConstantPool()));//expStatVisitor);
			codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			codegen.setVisitorForNode(ForNode.class, new ForVisitor(codegen));
			codegen.setVisitorForNode(FlowControlNode.class, new FlowControlVisitor(codegen));
			
			codegen.enterScope(symbols);
			methodNode.visitChildren(codegen);
			codegen.exitScope();
			
			// return null in case a return has not yet been hit
			assembler.pushNull();
			assembler._return();
		}
		
		method.setConstantPool(assembler.getConstantPool());
		method.setCode(assembler.getCodeSegment());
		method.setLocalCount(symbols.getLocalMax());
	}
	
	public CMethod getMethod(){
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return methodNode.getSymbol();
	}

}
