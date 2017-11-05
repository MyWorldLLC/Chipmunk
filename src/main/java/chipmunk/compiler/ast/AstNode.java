package chipmunk.compiler.ast;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public boolean hasChildren(){
		return children.size() > 0;
	}
	
	protected void addChild(AstNode child){
		children.add(child);
	}
	
	protected void addChildren(AstNode... children){
		this.children.addAll(Arrays.asList(children));
	}
	
	protected void addChildFirst(AstNode child){
		children.add(0, child);
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
	
	public void visit(AstVisitor visitor){
		
		if(visitor.preVisit(this)){
			// only visit children if preVisit() returns true
			for(AstNode child : children){
				child.visit(visitor);
			}
		}
		visitor.postVisit(this);
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
