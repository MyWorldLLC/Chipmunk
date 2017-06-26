package chipmunk.compiler.ir;

import java.util.ArrayDeque;
import java.util.Deque;

import chipmunk.compiler.Token;


public class MethodBlock extends ParentBlock {

	protected String name;
	protected Deque<Token> exp;
	protected boolean shared;
	protected boolean isFinal;
	
	public MethodBlock(){
		super();
		name = "";
		exp = new ArrayDeque<Token>();
	}
	
	public MethodBlock(String name){
		this();
		this.name = name;
	}
	
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
}
