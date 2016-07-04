package chipmunk.compiler;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
	
	public enum Type {
		ADD, SUB, MUL, DIV, FDIV, EXP, MOD, GETATTR, SETATTR, GETAT, SETAT, ASSIGN, LOOP, IF, ELSE,
		TRY, EXCEPT, CLASSDEF, METHODDEF, METHODCALL, IMPORT, IDENTIFIER, MODULE, NONTYPE
	}

	protected Type type;
	protected Token[] tokens;
	protected List<ASTNode> children;
	
	public ASTNode(){
		tokens = new Token[0];
		type = Type.NONTYPE;
		children = new ArrayList<ASTNode>();
	}
	
	public ASTNode(Token[] sourceTokens){
		tokens = sourceTokens;
		type = Type.NONTYPE;
		children = new ArrayList<ASTNode>();
	}
	
	public ASTNode(Type nodeType){
		tokens = new Token[0];
		type = nodeType;
		children = new ArrayList<ASTNode>();
	}
	
	public ASTNode(Type nodeType, Token[] sourceTokens){
		type = nodeType;
		tokens = sourceTokens;
		children = new ArrayList<ASTNode>();
	}
	
	public Token[] getTokens(){
		return tokens;
	}
	
	public void setTokens(Token[] t){
		tokens = t;
	}
	
	public void setType(Type t){
		type = t;
	}
	
	public Type getType(){
		return type;
	}
	
	public List<ASTNode> getChildren(){
		return children;
	}
	
	public void addChild(ASTNode child){
		children.add(child);
	}
}
