package chipmunk.compiler.ast;

public interface AstVisitor {
	
	public boolean preVisit(AstNode node);
	public void postVisit(AstNode node);

}
