package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.ast.VarDecNode;
import chipmunk.modules.lang.CClassType;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CModule;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;
	protected Codegen initCodegen;
	protected ChipmunkAssembler assembler;
	protected List<Object> constantPool;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<Object>();
		assembler = new ChipmunkAssembler(constantPool);
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			
			ModuleNode moduleNode = (ModuleNode) node;
			module = new CModule(moduleNode.getName(), constantPool);
			moduleNode.visitChildren(this);
			initCodegen = new Codegen(assembler, moduleNode.getSymbolTable());
			
		}else if(node instanceof ClassNode){
			// TODO
			ClassVisitor visitor = new ClassVisitor();
			node.visit(visitor);
			CClassType classType = visitor.getCClassType();
			
			module.getNamespace().set(classType.getName(), classType);
			
		}else if(node instanceof MethodNode){
			
			MethodVisitor visitor = new MethodVisitor(constantPool);
			node.visit(visitor);
			CMethod method = visitor.getMethod();
			
			method.bind(module);
			
			module.getNamespace().set(visitor.getMethodSymbol().getName(), method);
			
		}else if(node instanceof ImportNode){
			
			ImportNode importNode = (ImportNode) node;
			boolean importAll = importNode.isImportAll();
			
			CModule.Import im = module.new Import(importNode.getModule(), importAll);
			
			if(!importAll){
				im.getSymbols().addAll(importNode.getSymbols());
				im.getAliases().addAll(importNode.getAliases());
			}
			
			module.getImports().add(im);
		}else if(node instanceof VarDecNode){
			
			VarDecNode varDec = (VarDecNode) node;
			
			VarDecVisitor visitor = new VarDecVisitor(initCodegen);
			visitor.visit(varDec);
		}else{
			throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public CModule getModule(){
		
		CMethod initializer = new CMethod();
		
		initializer.setConstantPool(assembler.getConstantPool());
		initializer.setCode(assembler.getCodeSegment());
		initializer.setLocalCount(0);
		
		module.setInitializer(initializer);
		
		return module;
	}

}
