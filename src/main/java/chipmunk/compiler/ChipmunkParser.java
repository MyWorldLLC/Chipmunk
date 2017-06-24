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
		startBlock(module);
		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		
		Token next = tokens.peek();
		Token.Type nextType = next.getType();
		while(nextType != Token.Type.EOF){
			
			skipNewlines();
			
			if(nextType == Token.Type.MODULE){
				
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
		endBlock(module);
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
		startBlock(block);
		block.setName(id.getText());
		
		while(!peek(Token.Type.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			skipNewlines();
			
			boolean shared = false;
			if(peek(Token.Type.SHARED)){
				forceNext(Token.Type.SHARED);
				shared = true;
			}
			
			if(checkVarDec()){
				if(shared){
					block.addChild(new SharedBlock(parseVarDec()));
				}else{
					block.addChild(parseVarDec());
				}
			}else if(checkMethodDef()){
				if(shared){
					block.addChild(new SharedBlock(parseMethodDef()));
				}else{
					block.addChild(parseMethodDef());
				}
			}else{
				SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class body");
				error.setExpected(new Token.Type[]{Token.Type.VAR, Token.Type.DEF});
				error.setGot(tokens.peek());
				throw error;
			}
		}
		forceNext(Token.Type.RBRACE);
		endBlock(block);
		return block;
	}
	
	public boolean checkMethodDef(){
		return peek(Token.Type.DEF);
	}
	
	public MethodBlock parseMethodDef(){
		// statements & method definitions
		return null;
	}
	
	public boolean checkVarDec(){
		return peek(1, Token.Type.VAR) && peek(2, Token.Type.IDENTIFIER);
	}
	
	public VarDecBlock parseVarDec(){
		forceNext(Token.Type.VAR);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		VarDecBlock dec = new VarDecBlock();
		startBlock(dec);
		dec.setName(id.getText());
		
		if(peek(Token.Type.EQUALS)){
			tokens.get();
			dec.addChild(parseExpression());
		}
		
		endBlock(dec);
		return dec;
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
	
	private void skipNewlines(){
		while(dropNext(Token.Type.NEWLINE)){}
	}
	
	private boolean dropNext(Token.Type type){
		if(peek(type)){
			tokens.get();
			return true;
		}
		
		return false;
	}
	
	private boolean peek(Token.Type type){
		Token token = tokens.peek();
		
		return token.getType() == type;
	}
	
	private boolean peek(int places, Token.Type type){
		Token token = tokens.peek(places);
		return token.getType() == type;
	}
	
	private void startBlock(Block block){
		block.setTokenBeginIndex(tokens.getStreamPosition());
	}
	
	private void endBlock(Block block){
		block.setTokenEndIndex(tokens.getStreamPosition());
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
