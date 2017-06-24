package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ListBlock extends Block {

	protected List<ExpressionBlock> elements;
	
	public ListBlock(){
		elements = new ArrayList<ExpressionBlock>();
	}
	
	public List<ExpressionBlock> getElements(){
		return elements;
	}
	
	public void addElement(ExpressionBlock element){
		elements.add(element);
	}
}
