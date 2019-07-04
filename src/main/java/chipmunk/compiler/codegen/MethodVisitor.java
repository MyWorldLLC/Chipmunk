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
import chipmunk.compiler.ast.TryCatchNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.compiler.ast.WhileNode;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;

public class MethodVisitor implements AstVisitor {

	protected CMethod method;
	protected ChipmunkAssembler assembler;
	protected Codegen outerCodegen;
	protected SymbolTable symbols;
	protected Codegen codegen;
	protected MethodNode methodNode;
	
	protected CModule module;
	
	protected boolean defaultReturn;
	
	protected boolean isInner;
	
	
	public MethodVisitor(ChipmunkAssembler assembler, CModule module){
		this.assembler = assembler;
		defaultReturn = true;
		isInner = false;
		this.module = module;
	}
	
	public MethodVisitor(List<Object> constantPool, CModule module){
		assembler = new ChipmunkAssembler(constantPool);
		defaultReturn = true;
		isInner = false;
		this.module = module;
	}
	
	public MethodVisitor(Codegen outerCodegen, List<Object> constantPool, CModule module){
		assembler = new ChipmunkAssembler(constantPool);
		this.outerCodegen = outerCodegen;
		defaultReturn = true;
		isInner = true;
		this.module = module;
	}
	
	@Override
	public void visit(AstNode node) {
		
		method = new CMethod();
		
		if(node instanceof MethodNode){
			methodNode = (MethodNode) node;
			
			method.getCode().setArgCount(methodNode.getParamCount());
			method.getCode().setDefaultArgCount(methodNode.getDefaultParamCount());
			
			symbols = methodNode.getSymbolTable();
			
			codegen = new Codegen(assembler, symbols, module);
			
			ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);
			
			codegen.setVisitorForNode(OperatorNode.class, expStatVisitor);
			codegen.setVisitorForNode(MethodNode.class, new MethodVisitor(codegen, assembler.getConstantPool(), module));
			//codegen.setVisitorForNode(ClassNode.class, new ClassVisitor(assembler.getConstantPool(), module, assembler));
			codegen.setVisitorForNode(VarDecNode.class, new VarDecVisitor(codegen));
			codegen.setVisitorForNode(IfElseNode.class, new IfElseVisitor(codegen));
			codegen.setVisitorForNode(WhileNode.class, new WhileVisitor(codegen));
			codegen.setVisitorForNode(ForNode.class, new ForVisitor(codegen));
			codegen.setVisitorForNode(FlowControlNode.class, new FlowControlVisitor(codegen));
			codegen.setVisitorForNode(TryCatchNode.class, new TryCatchVisitor(codegen));
			
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
			
			// non-lambda methods are declared using statement block syntax. To support this, the result of assembling an
			// inner method must be saved as a local variable in the containing method.
			// TODO - forbid empty inner method names for non-lambda methods.
			/*if(isInner) {
				outerCodegen.getAssembler().push(getMethod());
				outerCodegen.emitSymbolAssignment(getMethodSymbol().getName());
			}*/
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
		method.getCode().setConstantPool(assembler.getConstantPool().toArray());
		method.getCode().setCode(assembler.getCodeSegment());
		method.getCode().setLocalCount(symbols.getLocalMax());
		method.getCode().setModule(module);
		return method;
	}
	
	public Symbol getMethodSymbol(){
		return methodNode.getSymbol();
	}

}
