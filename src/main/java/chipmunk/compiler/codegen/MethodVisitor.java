package chipmunk.compiler.codegen;

import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.FlowControlNode;
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
			codegen.setVisitorForNode(OperatorNode.class, new ExpressionStatementVisitor(codegen));
			codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			
			// TODO - clean this up
			codegen.setVisitorForNode(FlowControlNode.class, this);
			
			// TODO - parameter declarations
			if(methodNode.getChildren().size() == 0){
				assembler.pushNull();
				assembler._return();
			}
			node.visitChildren(codegen);
			
			// return null if a return has not yet been hit
			assembler.pushNull();
			assembler._return();
		}else if(node instanceof FlowControlNode){
			FlowControlNode flowNode = (FlowControlNode) node;
			
			if(flowNode.getControlToken().getType() == Token.Type.RETURN){
				if(node.hasChildren()){
					node.visitChildren(new ExpressionVisitor(assembler, symbols));
				}else{
					assembler.pushNull();
				}
				assembler._return();
			}else if(flowNode.getControlToken().getType() == Token.Type.THROW){
				node.visitChildren(new ExpressionVisitor(assembler, symbols));
				assembler._throw();
			}else if(flowNode.getControlToken().getType() == Token.Type.BREAK){
				// TODO
			}else if(flowNode.getControlToken().getType() == Token.Type.CONTINUE){
				// TODO
			}
			return;
		}else{
			node.visit(codegen);
			return;
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
