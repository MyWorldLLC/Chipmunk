package chipmunk.compiler.codegen;

import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.FlowControlNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.OperatorNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.modules.lang.CCode;
import chipmunk.modules.lang.CMethod;
import chipmunk.modules.lang.CObject;

public class MethodVisitor implements AstVisitor {

	protected CMethod method;
	protected ChipmunkAssembler assembler;
	protected SymbolTable symbols;
	protected MethodNode methodNode;
	
	public MethodVisitor(List<CObject> constantPool){
		assembler = new ChipmunkAssembler(constantPool);
	}
	
	@Override
	public boolean preVisit(AstNode node) {
		if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			symbols = methodNode.getSymbolTable();
			// TODO - parameter declarations
			if(methodNode.getChildren().size() == 0){
				assembler.pushNull();
				assembler._return();
			}
			return true;
		}else if(node instanceof OperatorNode){
			OperatorNode op = (OperatorNode) node;
			op.visit(new ExpressionVisitor(assembler, symbols));
			return false;
		}else if(node instanceof VarDecNode){
			VarDecNode dec = (VarDecNode) node;
			symbols.setSymbol(new Symbol(dec.getVarName(), 1)); // TODO - local var indices
			
			if(dec.getAssignExpr() != null){
				dec.getAssignExpr().visit(new ExpressionVisitor(assembler, symbols));
				assembler.setLocal(1);
			}
			return false;
		}else if(node instanceof FlowControlNode){
			FlowControlNode flowNode = (FlowControlNode) node;
			
			if(flowNode.getControlToken().getType() == Token.Type.RETURN){
				if(node.hasChildren()){
					node.visit(new ExpressionVisitor(assembler, symbols));
				}else{
					assembler.pushNull();
				}
				assembler._return();
			}else if(flowNode.getControlToken().getType() == Token.Type.THROW){
				node.visit(new ExpressionVisitor(assembler, symbols));
				assembler._throw();
			}else if(flowNode.getControlToken().getType() == Token.Type.BREAK){
				// TODO
			}else if(flowNode.getControlToken().getType() == Token.Type.CONTINUE){
				// TODO
			}
			
			return false;
		}else{
			return false;
		}
	}

	@Override
	public void postVisit(AstNode node) {
		method = new CMethod();
		method.setConstantPool(assembler.getConstantPool());
		method.setCode(new CCode(assembler.getCodeSegment()));
		// TODO - set proper local count
		method.setLocalCount(2);
	}
	
	public CMethod getMethod(){
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return null;
	}

}
