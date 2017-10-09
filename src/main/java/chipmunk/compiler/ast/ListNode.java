package chipmunk.compiler.ast;

public class ListNode extends AstNode {
	
	public ListNode(){
		super();
	}
	
	public void addChild(AstNode child){
		super.addChild(child);
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(list ");
		
		for(AstNode child : children){
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}
}
