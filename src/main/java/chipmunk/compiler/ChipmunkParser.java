package chipmunk.compiler;

import java.util.ArrayList;
import java.util.List;

import chipmunk.compiler.ir.Block;
import chipmunk.compiler.ir.ClassBlock;
import chipmunk.compiler.ir.ExpressionBlock;
import chipmunk.compiler.ir.ImportBlock;
import chipmunk.compiler.ir.ListBlock;
import chipmunk.compiler.ir.MethodBlock;
import chipmunk.compiler.ir.ModuleBlock;
import chipmunk.compiler.ir.SharedBlock;
import chipmunk.compiler.ir.VarDecBlock;

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
				endBlock(module);
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
		skipNewlines();
		
		forceNext(Token.Type.CLASS);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		ClassBlock block = new ClassBlock(module.getScope());
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
				syntaxError("Error parsing class body", tokens.peek(), Token.Type.VAR, Token.Type.DEF);
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
	
	public ListBlock parseList(){
		skipNewlines();
		
		ListBlock block = new ListBlock();
		startBlock(block);
		
		forceNext(Token.Type.LBRACKET);
		skipNewlines();
		
		while(!peek(Token.Type.RBRACKET)){
			skipNewlines();
			
			ExpressionBlock element = parseExpression();
			block.addElement(element);
			skipNewlines();
			
			if(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
			}else if(!peek(Token.Type.RBRACKET)){
				syntaxError("Invalid list", tokens.get(), Token.Type.COMMA, Token.Type.RBRACKET);
			}
			
			skipNewlines();
		}
		
		forceNext(Token.Type.RBRACKET);
		
		endBlock(block);
		return block;
	}
	
	public ExpressionBlock parseExpression(){
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
		return nextType == Token.Type.FROM || nextType == Token.Type.IMPORT;
	}
	
	/**
	 * Consumes the next import statement from the token stream.
	 * @return the import block for the statement
	 */
	public ImportBlock parseImport(){
		skipNewlines();
		ImportBlock block = new ImportBlock();
		startBlock(block);
		
		if(peek(Token.Type.IMPORT)){
			dropNext(Token.Type.IMPORT);
			// import single symbol
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(Token.Type.IDENTIFIER));
			
			while(peek(Token.Type.DOT)){
				dropNext(Token.Type.DOT);
				if(peek(Token.Type.IDENTIFIER)){
					identifiers.add(getNext(Token.Type.IDENTIFIER));
				}else if(peek(Token.Type.STAR)){
					identifiers.add(getNext(Token.Type.STAR));
				}else{
					throw new IllegalImportChipmunk("Expected identifier or *, got " + tokens.peek().getText());
				}
			}
			
			block.addSymbol(identifiers.get(identifiers.size() - 1).getText());
			
			// piece module name back together
			StringBuilder moduleName = new StringBuilder();
			for(int i = 0; i < identifiers.size() - 1; i++){
				moduleName.append(identifiers.get(i).getText());
				if(i < identifiers.size() - 2){
					moduleName.append('.');
				}
			}
			block.setModule(moduleName.toString());
		}else if(peek(Token.Type.FROM)){
			dropNext(Token.Type.FROM);
			
			// import multiple symbols
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(Token.Type.IDENTIFIER));
			
			while(peek(Token.Type.DOT)){
				dropNext(Token.Type.DOT);
				if(peek(Token.Type.IDENTIFIER)){
					identifiers.add(getNext(Token.Type.IDENTIFIER));
				}else if(peek(Token.Type.STAR)){
					identifiers.add(getNext(Token.Type.STAR));
				}else{
					throw new IllegalImportChipmunk("Expected identifier or *, got " + tokens.peek().getText());
				}
			}
			
			StringBuilder moduleName = new StringBuilder();
			for(int i = 0; i < identifiers.size(); i++){
				moduleName.append(identifiers.get(i).getText());
				if(i < identifiers.size() - 1){
					moduleName.append('.');
				}
			}
			block.setModule(moduleName.toString());
			
			forceNext(Token.Type.IMPORT);
			block.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				block.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			}
			block.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			
			if(block.getSymbols().contains("*") && block.getSymbols().size() > 1){
				throw new IllegalImportChipmunk("Cannot import multiple symbols and *");
			}
			
		}else{
			syntaxError("Invalid import", tokens.get(), Token.Type.IMPORT, Token.Type.FROM);
		}
		
		if(peek(Token.Type.AS)){
			
			if(block.getSymbols().contains("*")){
				throw new IllegalImportChipmunk("Cannot alias a * import");
			}
			
			dropNext(Token.Type.AS);
			// parse aliases
			block.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				block.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			}
			block.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			
			if(block.getSymbols().size() < block.getAliases().size()){
				throw new IllegalImportChipmunk("Cannot have more aliases than imported symbols");
			}
			
		}
		
		endBlock(block);
		return block;
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
	
	private void syntaxError(String msg, Token got, Token.Type... expected) throws SyntaxErrorChipmunk {
		SyntaxErrorChipmunk error = new SyntaxErrorChipmunk(msg);
		error.setExpected(expected);
		error.setGot(got);
		throw error;
	}
}
