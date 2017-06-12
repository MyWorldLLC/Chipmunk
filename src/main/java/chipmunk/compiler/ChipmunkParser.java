package chipmunk.compiler;

import java.util.List;

import chipmunk.compiler.ir.*;

public class ChipmunkParser {
	
	protected TokenStream tokens;
	protected List<ModuleBlock> modules;
	
	private ModuleBlock module;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
	}
	
	/**
	 * Parses all modules in the source stream.
	 */
	public void parse(){
		while(tokens.remaining() > 0){
			parseModule();
		}
	}
	
	public void parseModule(){
		module = new ModuleBlock();
		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		
		Token next = tokens.peek();
		Token.Type nextType = next.getType();
		while(nextType != Token.Type.EOF){
			
			if(nextType == Token.Type.NEWLINE){
				
				tokens.skip(1);
				
			}else if(nextType == Token.Type.MODULE){
				
				// add current module block to list and create new module block
				modules.add(module);
				module = new ModuleBlock();
				
			}else if(checkImport()){
				
				module.addImport(parseImport());
				
			}else if(checkVarDec()){
				
				module.addVariableDeclaration(parseVarDec());
				
			}else if(checkMethodDef()){
				
				module.addMethodDeclaration(parseMethodDef());
				
			}else if(checkClassDef()){
				
				module.addClassDeclaration(parseClassDef());
				
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
	
	public ClassBlock parseClassDef(){
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
	
	public MethodBlock parseMethodDef(){
		// statements & method definitions
		return null;
	}
	
	public boolean checkVarDec(){
		if(tokens.peek(1).getType() == Token.Type.VAR && tokens.peek(2).getType() == Token.Type.IDENTIFIER){
			return true;
		}else{
			return false;
		}
	}
	
	public VarDecBlock parseVarDec(){
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
	
	public Block parseVarSet(){
		return null;
	}
	
	public boolean checkExpression(){
		
		return true;
	}
	
	public Block parseExpression(){
		return null;
	}
	
	public boolean checkStatement(){
		return true;
	}
	
	public Block parseStatement(){
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
	public ImportBlock parseImport(){
		return null;
	}

}
