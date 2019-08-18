package chipmunk.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.parselets.*;

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
	protected String fileName;
	
	private Map<Token.Type, InfixParselet> infix;
	private Map<Token.Type, PrefixParselet> prefix;
	
	private List<ModuleNode> moduleRoots;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
		fileName = "";
		moduleRoots = new ArrayList<ModuleNode>();
		
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
		//register(Token.Type.CLASS, new ClassDefParselet());
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
	
	public void setFileName(String name){
		fileName = name;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * Parses all modules in the source stream.
	 */
	public void parse(){
		while(!peek(Token.Type.EOF)){
			moduleRoots.add(parseModule());
		}
	}
	
	public List<ModuleNode> getModuleRoots(){
		return moduleRoots;
	}
	
	public ModuleNode parseModule(){
		ModuleNode module = new ModuleNode();
		startNode(module);
		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		skipNewlinesAndComments();
		
		if(peek(Token.Type.MODULE)){
			forceNext(Token.Type.MODULE);
			
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(Token.Type.IDENTIFIER));
			
			while(peek(Token.Type.DOT)){
				dropNext();
				if(peek(Token.Type.IDENTIFIER)){
					identifiers.add(getNext(Token.Type.IDENTIFIER));
				}else{
					syntaxError("module", tokens.peek(), Token.Type.IDENTIFIER);
				}
			}
			
			// piece module name back together
			StringBuilder moduleName = new StringBuilder();
			if(identifiers.size() == 1){
				moduleName.append(identifiers.get(0).getText());
			}else{
				for(int i = 0; i < identifiers.size(); i++){
					moduleName.append(identifiers.get(i).getText());
					if (i < identifiers.size() - 1) {
						moduleName.append('.');
					}
				}
			}
			module.getSymbol().setName(moduleName.toString());
		}else{
			module.getSymbol().setName("");
		}
		
		skipNewlinesAndComments();
		
		Token next = tokens.peek();
		Token.Type nextType = next.getType();
		while(nextType != Token.Type.EOF){
			if(checkImport()){
				
				module.addImport(parseImport());
				
			}else if(checkVarDec()){
				
				module.addVarDec(parseVarDec());
				
			}else if(checkMethodDef()){
				
				MethodNode node = parseMethodDef();
				node.setParentSymbolTable(module);
				module.addMethodDef(node);
				
			}else if(checkClassDef()){
				
				ClassNode node = parseClassDef();
				module.addClassDef(node);
				node.setParentSymbolTable(module);
				
			}else if(peek(Token.Type.MODULE)){
				// Start of next module. Return this module node.
				break;
			}else{
				// Wuh-oh. Couldn't match one of the above cases. Panic!
				Token got = peek();
				syntaxError("module", "module start, class or method def, or variable declaration", got);
			}
			
			skipNewlinesAndComments();
			
			next = tokens.peek();
			nextType = next.getType();
		}
		endNode(module);
		return module;
	}
	
	public boolean checkClassDef(){
		return checkClassDef(true);
	}
	
	public boolean checkClassDef(boolean allowFinal){
		if(allowFinal){
			return peek(Token.Type.FINAL, Token.Type.CLASS) || peek(Token.Type.CLASS);
		}else{
			return peek(Token.Type.CLASS);
		}
	}
	
	public ClassNode parseClassDef(){
		skipNewlines();
		
		ClassNode node = new ClassNode();
		startNode(node);
		
		forceNext(Token.Type.CLASS);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		node.setName(id.getText());
		
		forceNext(Token.Type.LBRACE);
		skipNewlinesAndComments();
		while(!peek(Token.Type.RBRACE)){
			// parse class body (only variable declarations and method/class definitions allowed)
			skipNewlinesAndComments();
			
			boolean shared = false;
			if(dropNext(Token.Type.SHARED)){
				shared = true;
			}
			
			Symbol symbol = new Symbol();
			symbol.setShared(shared);
			
			if(checkVarOrTraitDec()){
				VarDecNode varNode = parseVarOrTraitDec();
				varNode.getSymbol().setShared(shared);
				node.addChild(varNode);
			}else if(checkMethodDef()){
				MethodNode methodNode = parseMethodDef();
				methodNode.getSymbol().setShared(shared);
				node.addChild(methodNode);
			}else if(checkClassDef()){
				ClassNode classNode = parseClassDef();
				classNode.getSymbol().setShared(shared);
				node.addChild(classNode);
			}else if(peek(Token.Type.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().getText()), tokens.peek(), Token.Type.FINAL, Token.Type.VAR, Token.Type.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);
			
			skipNewlines();
			
			if(peek(Token.Type.EOF)){
				syntaxError(String.format("Expected } at %d:%d, got EOF",peek().getLine(), peek().getColumn()), peek());
			}
		}
		forceNext(Token.Type.RBRACE);
		endNode(node);
		return node;
	}
	
	public ClassNode parseAnonClassDef(){
		skipNewlines();
		
		ClassNode node = new ClassNode();
		startNode(node);
		
		node.setName("");
		
		forceNext(Token.Type.LBRACE);
		skipNewlinesAndComments();
		while(!peek(Token.Type.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			skipNewlinesAndComments();
			
			boolean shared = false;
			if(dropNext(Token.Type.SHARED)){
				shared = true;
			}
			
			Symbol symbol = new Symbol();
			symbol.setShared(shared);
			
			if(checkVarOrTraitDec()){
				VarDecNode varNode = parseVarOrTraitDec();
				varNode.getSymbol().setShared(shared);
				node.addChild(varNode);
			}else if(checkMethodDef()){
				MethodNode methodNode = parseMethodDef();
				methodNode.getSymbol().setShared(shared);
				node.addChild(methodNode);
			}else if(checkClassDef()){
				ClassNode classNode = parseClassDef();
				classNode.getSymbol().setShared(shared);
				node.addChild(classNode);
			}else if(peek(Token.Type.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().getText()), tokens.peek(), Token.Type.FINAL, Token.Type.VAR, Token.Type.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);
			
			skipNewlines();
			
			if(peek(Token.Type.EOF)){
				syntaxError(String.format("Expected } at %d:%d, got EOF",peek().getLine(), peek().getColumn()), peek());
			}
		}
		forceNext(Token.Type.RBRACE);
		endNode(node);
		return node;
	}
	
	public boolean checkMethodDef(){
		return checkMethodDef(true);
	}
	
	public boolean checkMethodDef(boolean allowFinal){
		if(allowFinal){
			return peek(Token.Type.FINAL, Token.Type.DEF) || peek(Token.Type.DEF);
		}else{
			return peek(Token.Type.DEF);
		}
	}
	
	public MethodNode parseMethodDef(){
		skipNewlinesAndComments();
		
		MethodNode node = new MethodNode();
		startNode(node);
		
		if(dropNext(Token.Type.FINAL)){
			node.getSymbol().setFinal(true);
		}
		
		forceNext(Token.Type.DEF);
		node.setName(getNext(Token.Type.IDENTIFIER).getText());
		
		forceNext(Token.Type.LPAREN);
		while(peek(Token.Type.IDENTIFIER)){
			VarDecNode param = new VarDecNode();
			param.setVar(new IdNode(getNext(Token.Type.IDENTIFIER)));
			if(peek(Token.Type.EQUALS)){
				dropNext();
				param.setAssignExpr(parseExpression());
			}
			dropNext(Token.Type.COMMA);
			node.addParam(param);
		}
		forceNext(Token.Type.RPAREN);
		skipNewlines();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(peek(Token.Type.LBRACE)) {
			parseBlockBody(node);
		}else {
			AstNode expression = parseExpression();
			node.addToBody(expression);
		}
		
		endNode(node);
		return node;
	}
	
	public MethodNode parseAnonMethodDef(){
		skipNewlinesAndComments();
		
		MethodNode node = new MethodNode();
		startNode(node);
		
		forceNext(Token.Type.LPAREN);
		while(peek(Token.Type.IDENTIFIER)){
			VarDecNode param = new VarDecNode();
			param.setVar(new IdNode(getNext(Token.Type.IDENTIFIER)));
			if(peek(Token.Type.EQUALS)){
				dropNext();
				param.setAssignExpr(parseExpression());
			}
			dropNext(Token.Type.COMMA);
			node.addParam(param);
			dropNext(Token.Type.COMMA);
		}
		forceNext(Token.Type.RPAREN);
		skipNewlines();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(peek(Token.Type.LBRACE)) {
			parseBlockBody(node);
		}else {
			AstNode expression = parseExpression();
			node.addToBody(expression);
		}
		
		endNode(node);
		return node;
	}
	
	public boolean checkVarDec(){
		return peek(Token.Type.VAR) || peek(Token.Type.FINAL, Token.Type.VAR);
	}
	
	public boolean checkVarOrTraitDec() {
		return peek(Token.Type.VAR)
				|| peek(Token.Type.TRAIT)
				|| peek(Token.Type.FINAL, Token.Type.VAR)
				|| peek(Token.Type.FINAL, Token.Type.TRAIT);
	}
	
	public VarDecNode parseVarOrTraitDec() {
		
		VarDecNode dec = new VarDecNode();
		startNode(dec);
		if(dropNext(Token.Type.FINAL)){
			dec.getSymbol().setFinal(true);
		}
		
		if(peek(Token.Type.TRAIT)) {
			dropNext();
			dec.getSymbol().setTrait(true);
		}else {
			forceNext(Token.Type.VAR);
		}
		Token id = getNext(Token.Type.IDENTIFIER);
		
		dec.setVar(new IdNode(id));
		
		if(peek(Token.Type.EQUALS)){
			forceNext(Token.Type.EQUALS);
			dec.setAssignExpr(parseExpression());
		}
		
		endNode(dec);
		return dec;
	}
	
	public VarDecNode parseVarDec(){
		VarDecNode dec = new VarDecNode();
		startNode(dec);
		if(dropNext(Token.Type.FINAL)){
			dec.getSymbol().setFinal(true);
		}
		
		forceNext(Token.Type.VAR);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		dec.setVar(new IdNode(id));
		
		if(peek(Token.Type.EQUALS)){
			forceNext(Token.Type.EQUALS);
			dec.setAssignExpr(parseExpression());
		}
		
		endNode(dec);
		return dec;
	}
	
	public AstNode parseStatement(){
		// Statements are:
		// (a) variable declarations and assignments
		// (b) method definitions
		// (d) block beginnings
		// (e) expressions (including assignments of existing variables)
		
		skipNewlinesAndComments();
		
		if(checkVarDec()){
			return parseVarDec();
		}else if(checkMethodDef()){
			return parseMethodDef();
		}
		//else if(checkClassDef()){
		//	return parseClassDef();
		//}
		else if(peek().getType().isKeyword()){
			// parse block or keyword statement
			Token token = peek();
			Token.Type type = token.getType();
			
			switch(type){
			case IF:
				return parseIfElse();
			case WHILE:
				return parseWhile();
			case FOR:
				return parseFor();
			case TRY:
				return parseTryCatch();
			case RETURN:
				FlowControlNode returnNode = new FlowControlNode();
				startNode(returnNode);
				dropNext();
				returnNode.setControlToken(token);
				
				if(!peek(Token.Type.NEWLINE)){
					returnNode.addControlExpression(parseExpression());
				}
				
				endNode(returnNode);
				return returnNode;
			case THROW:
				FlowControlNode throwNode = new FlowControlNode();
				startNode(throwNode);
				dropNext();
				throwNode.setControlToken(token);
				throwNode.addControlExpression(parseExpression());
				endNode(throwNode);
				return throwNode;
			case BREAK:
			case CONTINUE:
				dropNext();
				
				FlowControlNode node = new FlowControlNode();
				startNode(node);
				dropNext();
				node.setControlToken(token);
				endNode(node);
				return node;
			default:
				syntaxError("Unexpected token", token, Token.Type.IF, Token.Type.WHILE, Token.Type.FOR, Token.Type.TRY,
					Token.Type.RETURN, Token.Type.THROW, Token.Type.BREAK, Token.Type.CONTINUE);
			}
		}else if(!peek(Token.Type.EOF)){
			// it's an expression
			return parseExpression();
		}
		return null;
	}
	
	public IfElseNode parseIfElse(){
		IfElseNode node = new IfElseNode();
		startNode(node);
		
		// keep parsing if & else-if branches until something causes this to break
		while(true){
			// Parse if branch
			GuardedNode ifBranch = parseIfBranch();
			
			node.addGuardedBranch(ifBranch);
			
			skipNewlinesAndComments();
			
			if(dropNext(Token.Type.ELSE)){
				skipNewlines();
				// it's the else block
				if(peek(Token.Type.LBRACE)){
					BlockNode elseBlock = new BlockNode();
					
					parseBlockBody(elseBlock);
					node.setElseBranch(elseBlock);
					
					break;
				}
				// if peek() result is not '{', assume it's another if branch
				// and keep going
			}else{
				// it's the end of the if-else chain
				break;
			}
		}
		
		endNode(node);
		return node;
	}
	
	public GuardedNode parseIfBranch(){
		GuardedNode ifBranch = new GuardedNode();
		startNode(ifBranch);
		
		forceNext(Token.Type.IF);
		forceNext(Token.Type.LPAREN);
		skipNewlines();
		
		ifBranch.setGuard(parseExpression());
		
		skipNewlines();
		forceNext(Token.Type.RPAREN);
		
		parseBlockBody(ifBranch);
		
		endNode(ifBranch);
		return ifBranch;
	}
	
	public WhileNode parseWhile(){
		WhileNode node = new WhileNode();
		startNode(node);
		
		forceNext(Token.Type.WHILE);
		forceNext(Token.Type.LPAREN);
		
		skipNewlines();
		node.setGuard(parseExpression());
		
		skipNewlines();
		forceNext(Token.Type.RPAREN);
		
		parseBlockBody(node);
		
		endNode(node);
		return node;
	}
	
	public ForNode parseFor(){
		ForNode node = new ForNode();
		startNode(node);
		
		forceNext(Token.Type.FOR);
		forceNext(Token.Type.LPAREN);
		
		skipNewlines();
		dropNext(Token.Type.VAR);
		IdNode varID = new IdNode(getNext(Token.Type.IDENTIFIER));
		forceNext(Token.Type.IN);
		
		AstNode expr = parseExpression();
		
		node.setID(new VarDecNode(varID));
		node.setIter(expr);
		
		skipNewlines();
		forceNext(Token.Type.RPAREN);
		
		parseBlockBody(node);
		
		endNode(node);
		return node;
	}
	
	public void parseBlockBody(BlockNode node){
		skipNewlines();
		forceNext(Token.Type.LBRACE);
		
		while(!peek(Token.Type.RBRACE)){
			skipNewlinesAndComments();
			if(peek(Token.Type.RBRACE)){
				break;
			}
			node.addToBody(parseStatement());
			skipNewlinesAndComments();
			
			if(peek(Token.Type.EOF)){
				syntaxError("block body", peek(), Token.Type.RBRACE);
			}
		}

		forceNext(Token.Type.RBRACE);
	}
	
	public AstNode parseTryCatch(){
		TryCatchNode node = new TryCatchNode();
		startNode(node);
		
		forceNext(Token.Type.TRY);
		TryNode tryNode = new TryNode();
		
		startNode(tryNode);
		parseBlockBody(tryNode);
		endNode(tryNode);
		// add try block to body
		node.setTryBlock(tryNode);
		
		// parse and add catch blocks
		//while(true){
			// TODO - support finally blocks and typed multi-catch
		forceNext(Token.Type.CATCH);
		forceNext(Token.Type.LPAREN);
			
		CatchNode catchNode = new CatchNode();
		startNode(catchNode);
			
		// if catch block has type of exception, include in node
		if(peek(Token.Type.IDENTIFIER, Token.Type.IDENTIFIER)){
			catchNode.setExceptionType(getNext(Token.Type.IDENTIFIER));
		}
			
		catchNode.setExceptionName(getNext(Token.Type.IDENTIFIER));
		forceNext(Token.Type.RPAREN);
			
		parseBlockBody(catchNode);
			
		endNode(catchNode);
		node.addCatchBlock(catchNode);
			
			//if(!peek(Token.Type.CATCH)){
			//	break;
			//}
		//}
		endNode(node);
		return node;
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
					node.setImportAll(true);
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
					node.setImportAll(true);
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
			
			if(peek(Token.Type.IDENTIFIER)){
				node.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
				while(peek(Token.Type.COMMA)){
					dropNext(Token.Type.COMMA);
					node.addSymbol(getNext(Token.Type.IDENTIFIER).getText());
				}
			}else{
				forceNext(Token.Type.STAR);
				node.setImportAll(true);
				node.addSymbol("*");
			}
			
		}else{
			syntaxError("Invalid import", tokens.get(), Token.Type.IMPORT, Token.Type.FROM);
		}
		
		if(peek(Token.Type.AS)){
			
			if(node.getSymbols().contains("*")){
				throw new IllegalImportChipmunk("Cannot alias a * import");
			}
			
			dropNext();
			// parse aliases
			node.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				node.addAlias(getNext(Token.Type.IDENTIFIER).getText());
			}
			
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
			syntaxError("expression", "literal, id, or prefix operator", token);
		}
		
		AstNode left = prefixParser.parse(this, token);
		
		token = tokens.peek();
		while(minPrecedence < getPrecedence(token)){
			token = tokens.get();
			
			InfixParselet infixParser = infix.get(token.getType());
			
			if(infixParser == null){
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
			syntaxError("", token, type);
		}
		
		return token;
	}
	
	public void forceNext(Token.Type type){
		Token token = tokens.get();
		
		if(token.getType() != type){
			syntaxError("", token, type);
		}
	}
	
	public void skipNewlines(){
		while(dropNext(Token.Type.NEWLINE)){}
	}
	
	public void skipNewlinesAndComments(){
		while(dropNext(Token.Type.NEWLINE) || dropNext(Token.Type.COMMENT)){}
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
	
	public Token peek(int places){
		return tokens.peek(places);
	}
	
	public boolean peek(Token.Type type){
		Token token = tokens.peek();
		
		return token.getType() == type;
	}
	
	public boolean peek(int places, Token.Type type){
		return tokens.peek(places).getType() == type;
	}
	
	public boolean peek(Token.Type... types){
		for(int i = 0; i < types.length; i++){
			if(peek(i).getType() != types[i]){
				return false;
			}
		}
		return true;
	}
	
	private void startNode(AstNode node){
		node.setBeginTokenIndex(tokens.getStreamPosition());
	}
	
	private void endNode(AstNode node){
		node.setEndTokenIndex(tokens.getStreamPosition());
	}
	
	public void syntaxError(String context, Token got, Token.Type... expected) throws SyntaxErrorChipmunk {
		StringBuilder expectedTypes = new StringBuilder();
		
		for(int i = 0; i < expected.length; i++){
			expectedTypes.append(expected[i].toString().toLowerCase());
			
			if(i < expected.length - 2){
				expectedTypes.append(", ");
			}else if(i == expected.length - 2){
				expectedTypes.append(", or ");
			}
		}
		
		String msg = String.format("Error parsing %s at %s %d:%d: expected %s, got %s",
				context, fileName, got.getLine(), got.getColumn(), expectedTypes.toString(), got.getText());
		
		SyntaxErrorChipmunk error = new SyntaxErrorChipmunk(msg);
		error.setExpected(expected);
		error.setGot(got);
		throw error;
	}
	
	public void syntaxError(String context, String expected, Token got) throws SyntaxErrorChipmunk {
		String msg = String.format("Error parsing %s at %s %d:%d: expected %s, got %s",
				context, fileName, got.getLine(), got.getColumn(), expected, got.getText());
		SyntaxErrorChipmunk error = new SyntaxErrorChipmunk(msg);
		error.setExpected(new Token.Type[]{});
		error.setGot(got);
		throw error;
	}
}
