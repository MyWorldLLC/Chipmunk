package chipmunk.truffle.codegen;

import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.ast.AstNode;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.truffle.ast.MethodNode;

public class MethodVisitor implements TruffleAstVisitor<MethodNode> {

	protected CMethod method;
	protected SymbolTable symbols;
	protected TruffleCodegen codegen;
	protected chipmunk.compiler.ast.MethodNode methodNode;
	
	protected CModule module;
	
	protected boolean defaultReturn;
	
	
	public MethodVisitor(){
		defaultReturn = true;
		this.module = module;
	}
	
	public MethodVisitor(List<Object> constantPool, CModule module){
		defaultReturn = true;
		this.module = module;
	}
	
	@Override
	public MethodNode visit(AstNode node) {
		
		method = new CMethod();
		
		if(node instanceof chipmunk.compiler.ast.MethodNode){
			methodNode = (chipmunk.compiler.ast.MethodNode) node;
			
			method.setArgCount(methodNode.getParamCount());
			method.setDefaultArgCount(methodNode.getDefaultParamCount());
			
			symbols = methodNode.getSymbolTable();
			
			//codegen = new TruffleCodegen(assembler, symbols, module);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);
			
			//codegen.setVisitorForNode(OperatorNode.class, expStatVisitor);
			//codegen.setVisitorForNode(MethodNode.class, new MethodVisitor(assembler.getConstantPool(), module));
			//codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			//codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			//codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			//codegen.setVisitorForNode(ForNode.class, new ForVisitor(codegen));
			//codegen.setVisitorForNode(FlowControlNode.class, new FlowControlVisitor(codegen));
			
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
				//assembler._return();
			}else {
				// regular methods
				//methodNode.visitChildren(codegen, startAt);
			}
			codegen.exitScope();
			
			if(defaultReturn){
				// return null in case a return has not yet been hit
				genDefaultReturn();
			}
		}
		return null; // TODO
	}
	
	public void genSelfReturn(){
		//assembler.getLocal(0);
		//assembler._return();
	}
	
	public void genDefaultReturn(){
		//assembler.pushNull();
		//assembler._return();
	}
	
	public boolean willGenDefaultReturn(){
		return defaultReturn;
	}
	
	public void setDefaultReturn(boolean defaultReturn){
		this.defaultReturn = defaultReturn;
	}
	
	public CMethod getMethod(){
		method.setLocalCount(symbols.getLocalMax());
		method.setModule(module);
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return methodNode.getSymbol();
	}

}
