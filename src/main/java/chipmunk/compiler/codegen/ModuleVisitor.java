package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.modules.lang.CClassType;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CModule;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;
	protected List<Object> constantPool;
	
	public ModuleVisitor(){
		constantPool = new ArrayList<Object>();
	}

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			module = new CModule(((ModuleNode) node).getName(), constantPool);
			node.visitChildren(this);
		}else if(node instanceof ClassNode){
			ClassVisitor visitor = new ClassVisitor();
			node.visit(visitor);
			CClassType classType = visitor.getCClassType();
			
			module.getNamespace().set(classType.getName(), classType);
		}else if(node instanceof MethodNode){
			MethodVisitor visitor = new MethodVisitor(constantPool);
			node.visit(visitor);
			CMethod method = visitor.getMethod();
			
			module.getNamespace().set(visitor.getMethodSymbol().getName(), method);
		}
	}
	
	public CModule getModule(){
		return module;
	}

}
