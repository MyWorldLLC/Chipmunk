package chipmunk.compiler.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.modules.lang.CClassType;
import chipmunk.modules.lang.CModule;
import chipmunk.modules.reflectiveruntime.CMethod;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;

	@Override
	public void visit(AstNode node) {
		if(node instanceof ModuleNode){
			module = new CModule();
			module.setName(((ModuleNode) node).getName());
			node.visitChildren(this);
		}else if(node instanceof ClassNode){
			ClassVisitor visitor = new ClassVisitor();
			node.visit(visitor);
			CClassType classType = visitor.getCClassType();
			
			module.setAttribute(classType.getName(), classType);
		}else if(node instanceof MethodNode){
			// TODO - constant pool
			MethodVisitor visitor = new MethodVisitor(module.getConstantPool());
			node.visit(visitor);
			CMethod method = visitor.getMethod();
			
			// TODO - module.setAttribute(visitor.getMethodSymbol().getName(), method);
		}
	}
	
	public CModule getModule(){
		return module;
	}

}
