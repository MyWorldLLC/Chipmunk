package chipmunk.compiler;

public class ChipmunkParser {
	
	protected TokenStream tokens;
	protected ASTNode ast;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
	}
	
	public void parseModule(){
		// parse imports, literal assignments, class definitions, and method definitions
		
		ASTNode module = new ASTNode();
		module.setType(ASTNode.Type.MODULE);
		
		Token next = tokens.peek();
		Token.Type nextType = next.getType();
		while(nextType != Token.Type.EOF){
			
			if(nextType == Token.Type.NEWLINE){
				
				tokens.skip(1);
				
			}else if(checkImport()){
				
				module.addChild(parseImport());
				
			}else if(checkVarSet()){
				
				module.addChild(parseVarSet());
				
			}else if(checkMethodDef()){
				
				module.addChild(parseMethodDef());
				
			}else if(checkClassDef()){
				
				module.addChild(parseClassDef());
				
			}else{
				// Wuh-oh. Couldn't match one of the above cases. Panic!
			}
			
			next = tokens.peek();
			nextType = next.getType();
		}
		
	}
	
	public boolean checkClassDef(){
		if(tokens.peek().getType() == Token.Type.CLASS){
			return true;
		}else{
			return false;
		}
	}
	
	public ASTNode parseClassDef(){
		// assignments, class definitions, method definitions
		return null;
	}
	
	public boolean checkMethodDef(){
		Token.Type identifier = tokens.peek().getType();
		
		if(identifier == Token.Type.IDENTIFIER){
			
			if(tokens.peek(1).getType() == Token.Type.LPAREN){
				
				int parenCount = 1;
				int peekIndex = 2;

				Token peekToken = tokens.peek(peekIndex);
				Token.Type peekType = peekToken.getType();
				
				// count past nested parens to allow for nested expressions within method def
				while(parenCount > 0){
					
					if(peekType == Token.Type.LPAREN){
						parenCount++;
					}else if(peekType == Token.Type.RPAREN){
						parenCount--;
					}else if(peekType == Token.Type.EOF){
						throw new CompileChipmunk("Unexpected EOF at line " + peekToken.getLine() + ", column " + peekToken.getColumn());
					}
					peekIndex++;
					peekToken = tokens.peek(peekIndex);
					peekType = peekToken.getType();
				}
				
				// we've reached the end of any nested parens. Check for left curly brace
				
				// look past any newlines. Any non newline token following the parentheses
				// must be a left curly brace, or this was a method call, not a method definition
				while(peekType == Token.Type.NEWLINE){
					peekIndex++;
					peekToken = tokens.peek(peekIndex);
					peekType = peekToken.getType();
				}
				
				if(peekType == Token.Type.LBRACE){
					return true;
				}
				
			}
		}
		return false;
	}
	
	public ASTNode parseMethodDef(){
		// statements, class & method definitions
		return null;
	}
	
	public boolean checkVarSet(){
		
		if(tokens.peek(1).getType() == Token.Type.IDENTIFIER){
			
			Token.Type equalsOrNewline = tokens.peek(2).getType();
			if(equalsOrNewline == Token.Type.EQUALS || equalsOrNewline == Token.Type.NEWLINE){
				return true;
			}
		}
		return false;
	}
	
	public ASTNode parseVarSet(){
		return null;
	}
	
	public boolean checkExpression(){
		
		return true;
	}
	
	public ASTNode parseExpression(){
		return null;
	}
	
	public boolean checkStatement(){
		return true;
	}
	
	public ASTNode parseStatement(){
		return null;
	}
	
	/**
	 * Checks if the next token sequence is an import. Does not modify the token stream.
	 * @return true if the next token sequence should be parsed as an import, false if not
	 */
	public boolean checkImport(){
		
		Token.Type nextType = tokens.peek().getType();
		
		if(nextType == Token.Type.FROM || nextType == Token.Type.IMPORT){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Consumes the next import statement from the token stream.
	 * @return an AST of the next import statement
	 */
	public ASTNode parseImport(){
		return null;
	}

}
