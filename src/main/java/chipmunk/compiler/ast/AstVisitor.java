package chipmunk.compiler.ast;

public interface AstVisitor {
	
	public void preVisit(AstNode node);
	public void postVisit(AstNode node);

}
