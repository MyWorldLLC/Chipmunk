package chipmunk.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class AstNode {
	
	protected List<AstNode> children;
	protected int beginTokenIndex;
	protected int endTokenIndex;
	
	public AstNode(){
		children = new ArrayList<AstNode>();
	}
	
	public AstNode(AstNode... children){
		this();
		for(AstNode child : children){
			this.children.add(child);
		}
	}
	
	public List<AstNode> getChildren(){
		return children;
	}
	
	protected void addChild(AstNode child){
		children.add(child);
	}
	
	public int getBeginTokenIndex(){
		return beginTokenIndex;
	}
	
	public void setBeginTokenIndex(int index){
		beginTokenIndex = index;
	}
	
	public int getEndTokenIndex(){
		return beginTokenIndex;
	}
	
	public void setEndTokenIndex(int index){
		endTokenIndex = index;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		
		for(AstNode child : children){
			builder.append(child.toString());
		}
		
		builder.append(')');
		return builder.toString();
	}

}
