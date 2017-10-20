package chipmunk.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.ClassNode;
import chipmunk.compiler.ast.ImportNode;
import chipmunk.compiler.ast.MethodNode;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.ir.MethodBlock;
import chipmunk.compiler.ir.VarDecBlock;
import chipmunk.compiler.parselets.AddSubOperatorParselet;
import chipmunk.compiler.parselets.AndOperatorParselet;
import chipmunk.compiler.parselets.AssignOperatorParselet;
import chipmunk.compiler.parselets.BitAndOperatorParselet;
import chipmunk.compiler.parselets.BitOrOperatorParselet;
import chipmunk.compiler.parselets.BitXOrOperatorParselet;
import chipmunk.compiler.parselets.CallOperatorParselet;
import chipmunk.compiler.parselets.ClassDefParselet;
import chipmunk.compiler.parselets.DotOperatorParselet;
import chipmunk.compiler.parselets.EqualityOperatorParselet;
import chipmunk.compiler.parselets.GroupingParselet;
import chipmunk.compiler.parselets.IndexOperatorParselet;
import chipmunk.compiler.parselets.InfixParselet;
import chipmunk.compiler.parselets.LessGreaterOperatorParselet;
import chipmunk.compiler.parselets.ListParselet;
import chipmunk.compiler.parselets.LiteralParselet;
import chipmunk.compiler.parselets.MapParselet;
import chipmunk.compiler.parselets.MethodDefParselet;
import chipmunk.compiler.parselets.MulDivOperatorParselet;
import chipmunk.compiler.parselets.NameParselet;
import chipmunk.compiler.parselets.OrOperatorParselet;
import chipmunk.compiler.parselets.PostIncDecParselet;
import chipmunk.compiler.parselets.PowerOperatorParselet;
import chipmunk.compiler.parselets.PrefixOperatorParselet;
import chipmunk.compiler.parselets.PrefixParselet;
import chipmunk.compiler.parselets.ShiftRangeOperatorParselet;

/**
 * Parses the Chipmunk language using a Pratt parser design for expressions. Many thanks to 
 * Fredrik Lundh (http://effbot.org/zone/simple-top-down-parsing.htm) for his
 * extensive work in explaining the Pratt design and to
 * Bob Nystrom (http://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy/)
 * for his extensive work in explaining the design and providing a complete working Java example of a 
 * beautifully clean implementation of a Pratt parser for a complete example expression language.
 * 
 * Traditional recursive-descent is used to parse module, class, and method structures, which naturally
 * fit the recursive-descent paradigm.
 */
public class ChipmunkParser {
	
	protected TokenStream tokens;
	
	private Map<Token.Type, InfixParselet> infix;
	private Map<Token.Type, PrefixParselet> prefix;
	
	protected List<ModuleNode> modules;
	private ModuleNode module;
	
	private List<AstNode> moduleRoots;
	private AstNode root;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
		modules = new ArrayList<ModuleNode>();
		moduleRoots = new ArrayList<AstNode>();
		
		infix = new HashMap<Token.Type, InfixParselet>();
		prefix = new HashMap<Token.Type, PrefixParselet>();
		
		// register parselets
		
		// identifiers and literals
		register(Token.Type.IDENTIFIER, new NameParselet());
		register(Token.Type.BOOLLITERAL, new LiteralParselet());
		register(Token.Type.BINARYLITERAL, new LiteralParselet());
		register(Token.Type.HEXLITERAL, new LiteralParselet());
		register(Token.Type.OCTLITERAL, new LiteralParselet());
		register(Token.Type.INTLITERAL, new LiteralParselet());
		register(Token.Type.FLOATLITERAL, new LiteralParselet());
		register(Token.Type.STRINGLITERAL, new LiteralParselet());
		register(Token.Type.LBRACKET, new ListParselet());
		register(Token.Type.LBRACE, new MapParselet());
		
		// prefix operators
		prefixOp(Token.Type.PLUS);
		prefixOp(Token.Type.MINUS);
		prefixOp(Token.Type.DOUBLEPLUS);
		prefixOp(Token.Type.DOUBLEMINUS);
		prefixOp(Token.Type.EXCLAMATION);
		prefixOp(Token.Type.TILDE);
		
		// parentheses for grouping in expressions
		register(Token.Type.LPAREN, new GroupingParselet());
		
		// binary infix operators
		register(Token.Type.PLUS, new AddSubOperatorParselet());
		register(Token.Type.MINUS, new AddSubOperatorParselet());
		register(Token.Type.STAR, new MulDivOperatorParselet());
		register(Token.Type.FSLASH, new MulDivOperatorParselet());
		register(Token.Type.DOUBLEFSLASH, new MulDivOperatorParselet());
		register(Token.Type.PERCENT, new MulDivOperatorParselet());
		
		register(Token.Type.DOUBLESTAR, new PowerOperatorParselet());
		
		register(Token.Type.DOT, new DotOperatorParselet());
		
		register(Token.Type.DOUBLELESSTHAN, new ShiftRangeOperatorParselet());
		register(Token.Type.DOUBLEMORETHAN, new ShiftRangeOperatorParselet());
		register(Token.Type.DOUBLEDOTLESS, new ShiftRangeOperatorParselet());
		register(Token.Type.DOUBLEDOT, new ShiftRangeOperatorParselet());
		
		register(Token.Type.LESSTHAN, new LessGreaterOperatorParselet());
		register(Token.Type.LESSEQUALS, new LessGreaterOperatorParselet());
		register(Token.Type.MORETHAN, new LessGreaterOperatorParselet());
		register(Token.Type.MOREEQUALS, new LessGreaterOperatorParselet());
		
		register(Token.Type.DOUBLEEQUAlS, new EqualityOperatorParselet());
		register(Token.Type.EXCLAMATIONEQUALS, new EqualityOperatorParselet());
		
		register(Token.Type.AMPERSAND, new BitAndOperatorParselet());
		register(Token.Type.BAR, new BitOrOperatorParselet());
		register(Token.Type.CARET, new BitXOrOperatorParselet());
		
		register(Token.Type.DOUBLEAMPERSAND, new AndOperatorParselet());
		register(Token.Type.DOUBLEBAR, new OrOperatorParselet());
		
		register(Token.Type.EQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEPLUSEQUALS, new AssignOperatorParselet());
		register(Token.Type.PLUSEQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEMINUSEQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLESTAREQUALS, new AssignOperatorParselet());
		register(Token.Type.STAREQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEFSLASHEQUALS, new AssignOperatorParselet());
		register(Token.Type.FSLASHEQUALS, new AssignOperatorParselet());
		register(Token.Type.PERCENTEQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEAMPERSANDEQUALS, new AssignOperatorParselet());
		register(Token.Type.AMPERSANDEQUALS, new AssignOperatorParselet());
		register(Token.Type.CARETEQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEBAREQUALS, new AssignOperatorParselet());
		register(Token.Type.BAREQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLELESSEQUALS, new AssignOperatorParselet());
		register(Token.Type.LESSEQUALS, new AssignOperatorParselet());
		register(Token.Type.TRIPLEMOREQUALS, new AssignOperatorParselet());
		register(Token.Type.DOUBLEMOREEQUALS, new AssignOperatorParselet());
		register(Token.Type.MOREEQUALS, new AssignOperatorParselet());
		register(Token.Type.TILDEEQUALS, new AssignOperatorParselet());
		
		// postfix operators
		register(Token.Type.DOUBLEPLUS, new PostIncDecParselet());
		register(Token.Type.DOUBLEMINUS, new PostIncDecParselet());
		register(Token.Type.LPAREN, new CallOperatorParselet());
		register(Token.Type.LBRACKET, new IndexOperatorParselet());
		
		// method def operator (allow method definitions in expressions)
		register(Token.Type.DEF, new MethodDefParselet());
		// class definition operator (allows creating anonymous classes in expressions)
		register(Token.Type.CLASS, new ClassDefParselet());
	}
	
	protected void register(Token.Type type, InfixParselet parselet){
		infix.put(type, parselet);
	}
	
	protected void register(Token.Type type, PrefixParselet parselet){
		prefix.put(type, parselet);
	}
	
	protected void prefixOp(Token.Type op){
		prefix.put(op, new PrefixOperatorParselet());
	}
	
	public TokenStream getTokens(){
		return tokens;
	}
	
	/**
	 * Parses all modules in the source stream.
	 */
	public void parse(){
		while(tokens.remaining() > 0){
			parseModule();
		}
	}
	
	public ModuleNode parseModule(){
		module = new ModuleNode();
		startNode(module);
		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		forceNext(Token.Type.MODULE);
		module.setName(getNext(Token.Type.IDENTIFIER).getText());
		
		skipNewlines();
		
		Token next = tokens.peek();
		Token.Type nextType = next.getType();
		while(nextType != Token.Type.EOF){
			if(checkImport()){
				
				module.addImport(parseImport());
				
			}else if(checkVarDec()){
				
				//module.addChild(parseVarDec());
				
			}else if(checkMethodDef()){
				
				MethodNode node = parseMethodDef();
				node.setParentSymbolTable(module);
				module.addMethodDef(node);
				
			}else if(checkClassDef()){
				
				ClassNode node = parseClassDef();
				module.addClassDef(node);
				node.setParentSymbolTable(module);
				
			}else{
				// Wuh-oh. Couldn't match one of the above cases. Panic!
			}
			
			skipNewlines();
			
			next = tokens.peek();
			nextType = next.getType();
		}
		endNode(module);
		return module;
	}
	
	public boolean checkClassDef(){
		return tokens.peek().getType() == Token.Type.CLASS ? true : false;
	}
	
	public ClassNode parseClassDef(){
		skipNewlines();
		
		forceNext(Token.Type.CLASS);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		ClassNode node = new ClassNode();
		
		startNode(node);
		node.setName(id.getText());
		
		if(peek(Token.Type.EXTENDS)){
			dropNext(Token.Type.EXTENDS);
			node.setSuperName(getNext(Token.Type.IDENTIFIER).getText());
		}
		
		while(!peek(Token.Type.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			skipNewlines();
			
			boolean shared = false;
			if(peek(Token.Type.SHARED)){
				forceNext(Token.Type.SHARED);
				shared = true;
			}
			
			boolean isFinal = false;
			if(peek(Token.Type.FINAL)){
				forceNext(Token.Type.FINAL);
				isFinal = true;
			}
			
			if(checkVarDec()){
				VarDecBlock varBlock = parseVarDec();
				varBlock.setShared(shared);
				varBlock.setFinal(isFinal);
				//node.addChild(varBlock);
			}else if(checkMethodDef()){
				MethodBlock methodBlock = null;//parseMethodDef();
				methodBlock.setShared(shared);
				methodBlock.setFinal(isFinal);
				//node.addChild(methodBlock);
			}else{
				syntaxError("Error parsing class body", tokens.peek(), Token.Type.VAR, Token.Type.DEF);
			}
		}
		forceNext(Token.Type.RBRACE);
		endNode(node);
		return node;
	}
	
	public boolean checkMethodDef(){
		return peek(Token.Type.DEF);
	}
	
	public MethodNode parseMethodDef(){
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
		//startBlock(dec);
		dec.setName(id.getText());
		
		if(peek(Token.Type.EQUALS)){
			tokens.get();
			//dec.addChild(parseExpressionOld());
		}
		
		//endBlock(dec);
		return dec;
	}
	
	public AstNode parseStatement(){
		// statements are either (a) variable declarations and assignments
		// (b) expressions (including assignments)
		// or (c) block beginnings
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
	public ImportNode parseImport(){
		skipNewlines();
		ImportNode node = new ImportNode();
		startNode(node);
		
		if(peek(Token.Type.IMPORT)){
			dropNext(Token.Type.IMPORT);
			// import single symbol
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(Token.Type.IDENTIFIER));
			
			while(peek(Token.Type.DOT)){
				dropNext();
				if(peek(Token.Type.IDENTIFIER)){
					identifiers.add(getNext(Token.Type.IDENTIFIER));
				}else if(peek(Token.Type.STAR)){
					identifiers.add(getNext(Token.Type.STAR));
					break;
				}else{
					throw new IllegalImportChipmunk("Expected identifier or *, got " + tokens.peek().getText());
				}
			}
			
			// piece module name back together
			StringBuilder moduleName = new StringBuilder();
			if(identifiers.size() == 1){
				moduleName.append(identifiers.get(0).getText());
			}else{
				for(int i = 0; i < identifiers.size() - 1; i++){
					moduleName.append(identifiers.get(i).getText());
					if (i < identifiers.size() - 2) {
						moduleName.append('.');
					}
				}
			}
			
			if(identifiers.size() > 1){
				node.addSymbol(identifiers.get(identifiers.size() - 1).getText());
			}
			
			node.setModule(moduleName.toString());
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
			
			node.setModule(moduleName.toString());
			
			forceNext(Token.Type.IMPORT);
			node.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				node.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			}
			node.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
			
			if(node.getSymbols().contains("*") && node.getSymbols().size() > 1){
				throw new IllegalImportChipmunk("Cannot import multiple symbols and *");
			}
			
		}else{
			syntaxError("Invalid import", tokens.get(), Token.Type.IMPORT, Token.Type.FROM);
		}
		
		if(peek(Token.Type.AS)){
			
			if(node.getSymbols().contains("*")){
				throw new IllegalImportChipmunk("Cannot alias a * import");
			}
			
			dropNext(Token.Type.AS);
			// parse aliases
			node.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				node.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			}
			node.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			
			if(node.getSymbols().size() < node.getAliases().size()){
				throw new IllegalImportChipmunk("Cannot have more aliases than imported symbols");
			}
			
		}
		
		endNode(node);
		return node;
	}
	
	/**
	 * Parse expressions with precedence climbing algorithm
	 * @return AST of the expression
	 */
	public AstNode parseExpression(){
		return parseExpression(0);
	}
	
	public AstNode parseExpression(int minPrecedence){
		
		Token token = tokens.get();
		
		PrefixParselet prefixParser = prefix.get(token.getType());
		
		if(prefixParser == null){
			throw new SyntaxErrorChipmunk("Expected a literal, id, or prefix operator", token);
		}
		
		AstNode left = prefixParser.parse(this, token);
		
		token = tokens.peek();
		while(minPrecedence < getPrecedence(token)){
			token = tokens.get();
			
			InfixParselet infixParser = infix.get(token.getType());
			
			if(infixParser == null){
				throw new SyntaxErrorChipmunk("Expected an infix operator", token);
			}
			
			left = infixParser.parse(this, left, token);
			token = tokens.peek();
		}
		
		return left;
	}
	
	private int getPrecedence(Token token){
		InfixParselet parselet = infix.get(token.getType());
		if(parselet != null){
			return parselet.getPrecedence();
		}else{
			return 0;
		}
	}
	
	public Token getNext(Token.Type type){
		Token token = tokens.get();
		
		if(token.getType() != type){
			
			SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class");
			error.setExpected(new Token.Type[]{type});
			error.setGot(token);
			
			throw error;
		}
		
		return token;
	}
	
	public void forceNext(Token.Type type){
		Token token = tokens.get();
		
		if(token.getType() != type){
			
			SyntaxErrorChipmunk error = new SyntaxErrorChipmunk("Error parsing class");
			error.setExpected(new Token.Type[]{type});
			error.setGot(token);
			
			throw error;
		}
	}
	
	public void skipNewlines(){
		while(dropNext(Token.Type.NEWLINE)){}
	}
	
	public boolean dropNext(Token.Type type){
		if(peek(type)){
			tokens.get();
			return true;
		}
		
		return false;
	}
	
	public void dropNext(){
		tokens.get();
	}
	
	public Token peek(){
		return tokens.peek();
	}
	
	public boolean peek(Token.Type type){
		Token token = tokens.peek();
		
		return token.getType() == type;
	}
	
	public boolean peek(int places, Token.Type type){
		Token token = tokens.peek(places);
		return token.getType() == type;
	}
	
	private void startNode(AstNode node){
		node.setBeginTokenIndex(tokens.getStreamPosition());
	}
	
	private void endNode(AstNode node){
		node.setEndTokenIndex(tokens.getStreamPosition());
	}
	
	public void syntaxError(String msg, Token got, Token.Type... expected) throws SyntaxErrorChipmunk {
		SyntaxErrorChipmunk error = new SyntaxErrorChipmunk(msg);
		error.setExpected(expected);
		error.setGot(got);
		throw error;
	}
}
