package chipmunk.compiler.codegen;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.modules.lang.CClassType;
import chipmunk.modules.lang.CMethod;
import chipmunk.modules.lang.CModule;

public class ModuleVisitor implements AstVisitor {
	
	protected CModule module;

	@Override
	public boolean preVisit(AstNode node) {
		if(node instanceof ModuleNode){
			module = new CModule();
			module.setName(((ModuleNode) node).getName());
			return true;
		}else if(node instanceof ClassNode){
			ClassVisitor visitor = new ClassVisitor();
			node.visit(visitor);
			CClassType classType = visitor.getCClassType();
			
			module.setAttribute(classType.getName(), classType);
			return false;
		}else if(node instanceof MethodNode){
			MethodVisitor visitor = new MethodVisitor();
			node.visit(visitor);
			CMethod method = visitor.getMethod();
			
			module.setAttribute(visitor.getMethodSymbol().getName(), method);
			return false;
		}
		return false;
	}

	@Override
	public void postVisit(AstNode node) {
		
	}
	
	public CModule getModule(){
		return module;
	}

}
