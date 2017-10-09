package chipmunk.compiler.ast;

public class MapNode extends AstNode {

	public MapNode(){
		super();
	}
	
	public void addMapping(AstNode key, AstNode value){
		this.addChild(new AstNode(key, value));
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("(map ");
		
		for(AstNode child : children){
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}
}
