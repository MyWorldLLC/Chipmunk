package chipmunk.compiler.ast;

public class WhileNode extends GuardedNode {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(while");
		
		for(AstNode child : children) {
			builder.append(' ');
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}
}
