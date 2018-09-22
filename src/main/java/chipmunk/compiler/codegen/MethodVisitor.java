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
	
	protected boolean defaultReturn;
	
	
	public MethodVisitor(ChipmunkAssembler assembler){
		this.assembler = assembler;
		defaultReturn = true;
	}
	
	public MethodVisitor(List<Object> constantPool){
		assembler = new ChipmunkAssembler(constantPool);
		defaultReturn = true;
	}
	
	@Override
	public void visit(AstNode node) {
		
		method = new CMethod();
		
		if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			
			method.setArgCount(methodNode.getParamCount());
			method.setDefaultArgCount(methodNode.getDefaultParamCount());
			
			symbols = methodNode.getSymbolTable();
			
			codegen = new Codegen(assembler, symbols);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);
			
			codegen.setVisitorForNode(OperatorNode.class, expStatVisitor);
			codegen.setVisitorForNode(MethodNode.class, new MethodVisitor(assembler.getConstantPool()));
			codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			codegen.setVisitorForNode(ForNode.class, new ForVisitor(codegen));
			codegen.setVisitorForNode(FlowControlNode.class, new FlowControlVisitor(codegen));
			
			// The VM sets the locals for arguments for us - we only need to handle default arguments
			// that aren't supplied in the call.
			// TODO - this will overwrite all default arguments that were supplied. To support these
			// properly, generate code to determine number of arguments in call and jump to the right
			// point to initialize non-supplied arguments.
			int startAt = methodNode.getParamCount();
			if(methodNode.hasDefaultParams()){
				startAt -= methodNode.getDefaultParamCount();
				// TODO
			}
			
			codegen.enterScope(symbols);
			if(methodNode.getChildren().size() == methodNode.getParamCount() + 1 
					&& ExpressionVisitor.isExpressionNode(methodNode.getChildren().get(methodNode.getChildren().size() - 1))) {
				// this supports "lambda" methods - single expression methods that automatically return without the "return" keyword
				ExpressionVisitor visitor = new ExpressionVisitor(codegen);
				visitor.visit(methodNode.getChildren().get(methodNode.getChildren().size() - 1));
				assembler._return();
			}else {
				// regular methods
				methodNode.visitChildren(codegen, startAt);
			}
			codegen.exitScope();
			
			if(defaultReturn){
				// return null in case a return has not yet been hit
				genDefaultReturn();
			}
		}
		
	}
	
	public void genSelfReturn(){
		assembler.getLocal(0);
		assembler._return();
	}
	
	public void genDefaultReturn(){
		assembler.pushNull();
		assembler._return();
	}
	
	public boolean willGenDefaultReturn(){
		return defaultReturn;
	}
	
	public void setDefaultReturn(boolean defaultReturn){
		this.defaultReturn = defaultReturn;
	}
	
	public CMethod getMethod(){
		method.setConstantPool(assembler.getConstantPool());
		method.setCode(assembler.getCodeSegment());
		method.setLocalCount(symbols.getLocalMax());
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return methodNode.getSymbol();
	}

}
