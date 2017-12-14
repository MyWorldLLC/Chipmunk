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
		if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			symbols = methodNode.getSymbolTable();
			
			codegen = new Codegen(assembler, symbols);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);
			
			codegen.setVisitorForNode(OperatorNode.class, expStatVisitor);
			codegen.setVisitorForNode(MethodNode.class, expStatVisitor);
			codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			codegen.setVisitorForNode(ForNode.class, new ForVisitor(codegen));
			codegen.setVisitorForNode(FlowControlNode.class, new FlowControlVisitor(codegen));
			
			// TODO - parameter declarations
			if(methodNode.getChildren().size() == 0){
				assembler.pushNull();
				assembler._return();
			}
			codegen.enterScope(symbols);
			node.visitChildren(codegen);
			codegen.exitScope();
			
			// return null if a return has not yet been hit
			assembler.pushNull();
			assembler._return();
		}
		
		method = new CMethod();
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
