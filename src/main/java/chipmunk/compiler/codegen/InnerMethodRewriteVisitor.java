package chipmunk.compiler.codegen;

import chipmunk.compiler.Token;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.VarDecNode;

public class InnerMethodRewriteVisitor implements AstVisitor {
	
	private int nestingDepth;
	
	public InnerMethodRewriteVisitor() {
		nestingDepth = 0;
	}

	@Override
	public void visit(AstNode node) {

		for(int i = 0; i < node.getChildren().size(); i++) {
			AstNode nextChild = node.getChildren().get(i);
			final boolean rewrite = nextChild instanceof MethodNode && !((MethodNode) nextChild).getName().equals("") && nestingDepth > 0;
			
			if(nextChild instanceof MethodNode) {
				nestingDepth++;
			}
			
			nextChild.visit(this);
			
			if(rewrite) {
				VarDecNode dec = new VarDecNode();
				dec.getSymbol().setName(((MethodNode) nextChild).getName());
				dec.setVar(new IdNode(new Token(((MethodNode) nextChild).getName(), Token.Type.IDENTIFIER)));
				dec.setAssignExpr(nextChild);
				node.getChildren().set(i, dec);
				((MethodNode) nextChild).setName("");
			}
				
			if(nextChild instanceof MethodNode) {
				nestingDepth--;
			}
		}
	}

}
