package chipmunk.compiler.ir;

import java.util.ArrayDeque;
import java.util.Deque;

import chipmunk.compiler.Token;


public class MethodBlock extends ScopedBlock {

	protected String name;
	protected Deque<Token> exp;
	
	public MethodBlock(){
		super();
		exp = new ArrayDeque<Token>();
	}
	
	public MethodBlock(String name){
		this();
		this.name = name;
	}
}
