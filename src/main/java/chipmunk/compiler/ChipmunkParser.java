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
				
				module.addChild(parseImport());
				
			}else if(checkVarDec()){
				
				module.addChild(parseVarDec());
				
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
	
	public ClassBlock parseClassDef(){
		
		forceNext(Token.Type.CLASS);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		// TODO - support inheritance
		
		ClassBlock block = new ClassBlock();
		block.setName(id.getText());
		
		while(!peek(Token.Type.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			if(checkVarDec()){
				block.addChild(parseVarDec());
			}else if(checkMethodDef()){
				block.addChild(parseMethodDef());
			}else{
				SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class body");
				error.setExpected(new Token.Type[]{Token.Type.VAR, Token.Type.DEF});
				error.setGot(tokens.peek());
				throw error;
			}
		}
		forceNext(Token.Type.RBRACE);
		
		return block;
	}
	
	public boolean checkMethodDef(){
		return tokens.peek().getType() == Token.Type.DEF;
	}
	
	public MethodBlock parseMethodDef(){
		// statements & method definitions
		return null;
	}
	
	public boolean checkVarDec(){
		return tokens.peek(1).getType() == Token.Type.VAR && tokens.peek(2).getType() == Token.Type.IDENTIFIER;
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
	
	private Token getNext(Token.Type type){
		Token token = tokens.get();
		
		if(token.getType() != type){
			
			SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class");
			error.setExpected(new Token.Type[]{type});
			error.setGot(token);
			
			throw error;
		}
		
		return token;
	}
	
	private void forceNext(Token.Type type){
		Token token = tokens.get();
		
		if(token.getType() != type){
			
			SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class");
			error.setExpected(new Token.Type[]{type});
			error.setGot(token);
			
			throw error;
		}
	}
	
	private boolean peek(Token.Type type){
		Token token = tokens.peek();
		
		return token.getType() == type;
	}
	
	private boolean peek(int places, Token.Type type){
		Token token = tokens.peek(places);
		return token.getType() == type;
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
