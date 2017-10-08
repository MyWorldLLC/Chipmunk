package chipmunk.compiler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.IdNode;
import chipmunk.compiler.ast.LiteralNode;
import chipmunk.compiler.ir.Block;
import chipmunk.compiler.ir.ClassBlock;
import chipmunk.compiler.ir.ExpressionBlock;
import chipmunk.compiler.ir.ExpressionNode;
import chipmunk.compiler.ir.ImportBlock;
import chipmunk.compiler.ir.ListBlock;
import chipmunk.compiler.ir.MethodBlock;
import chipmunk.compiler.ir.ModuleBlock;
import chipmunk.compiler.ir.VarDecBlock;
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
	
	private Map<Token.Type, InfixParselet> infix;
	private Map<Token.Type, PrefixParselet> prefix;
	
	// TODO - deprecated
	protected List<ModuleBlock> modules;
	private ModuleBlock module;
	
	private List<AstNode> moduleRoots;
	private AstNode root;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
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
		// TODO - add list & map literals
		
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
				
				//module.addChild(parseMethodDef());
				
			}else if(checkClassDef()){
				
				//module.addChild(parseClassDef());
				
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
	
	public AstNode parseClassDef(){
		skipNewlines();
		
		forceNext(Token.Type.CLASS);
		Token id = getNext(Token.Type.IDENTIFIER);
		
		ClassBlock block = new ClassBlock(module.getScope());
		startBlock(block);
		block.setName(id.getText());
		
		if(peek(Token.Type.EXTENDS)){
			dropNext(Token.Type.EXTENDS);
			block.addSuperName(getNext(Token.Type.IDENTIFIER).getText());
			
			while(peek(Token.Type.COMMA)){
				dropNext(Token.Type.COMMA);
				block.addSuperName(getNext(Token.Type.IDENTIFIER).getText());
			}
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
				block.addChild(varBlock);
			}else if(checkMethodDef()){
				MethodBlock methodBlock = null;//parseMethodDef();
				methodBlock.setShared(shared);
				methodBlock.setFinal(isFinal);
				block.addChild(methodBlock);
			}else{
				syntaxError("Error parsing class body", tokens.peek(), Token.Type.VAR, Token.Type.DEF);
			}
		}
		forceNext(Token.Type.RBRACE);
		endBlock(block);
		return null;
	}
	
	public boolean checkMethodDef(){
		return peek(Token.Type.DEF);
	}
	
	public AstNode parseMethodDef(){
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
			dec.addChild(parseExpressionOld());
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
			
			ExpressionBlock element = parseExpressionOld();
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
	
	private ExpressionNode parseExpLevel12(){
		// Logical OR
		ExpressionNode left = parseExpLevel11();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOUBLEBAR)){
			op = tokens.get();
			right = parseExpLevel12();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel11(){
		// Logical AND
		ExpressionNode left = parseExpLevel10();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOUBLEAMPERSAND)){
			op = tokens.get();
			right = parseExpLevel11();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel10(){
		// Bitwise OR
		ExpressionNode left = parseExpLevel9();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.BAR)){
			op = tokens.get();
			right = parseExpLevel10();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel9(){
		// Bitwise XOR
		ExpressionNode left = parseExpLevel8();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.CARET)){
			op = tokens.get();
			right = parseExpLevel9();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel8(){
		// Bitwise AND
		ExpressionNode left = parseExpLevel7();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.AMPERSAND)){
			op = tokens.get();
			right = parseExpLevel8();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel7(){
		// == and !=
		ExpressionNode left = parseExpLevel6();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOUBLEEQUAlS) || peek(Token.Type.EXCLAMATIONEQUALS)){
			op = tokens.get();
			right = parseExpLevel7();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel6(){
		// <, >, <=, >=
		ExpressionNode left = parseExpLevel5();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.LESSTHAN) || peek(Token.Type.MORETHAN)
				|| peek(Token.Type.LESSEQUALS) || peek(Token.Type.MOREEQUALS)){
			op = tokens.get();
			right = parseExpLevel6();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel5(){
		// <<, >>, >>>, ..
		ExpressionNode left = parseExpLevel4();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOUBLELESSTHAN) || peek(Token.Type.DOUBLEMORETHAN)
				|| peek(Token.Type.TRIPLEMORETHAN) || peek(Token.Type.DOUBLEDOT)){
			op = tokens.get();
			right = parseExpLevel5();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel4(){
		// +, - (add, sub)
		ExpressionNode left = parseExpLevel3();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.PLUS) || peek(Token.Type.MINUS)){
			op = tokens.get();
			right = parseExpLevel4();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel3(){
		// *, /, //, %, ++ and -- (pre), +, -, !, ~ (unary)
		
		if(peek(Token.Type.DOUBLEPLUS) || peek(Token.Type.DOUBLEMINUS) || peek(Token.Type.PLUS)
				|| peek(Token.Type.MINUS) || peek(Token.Type.EXCLAMATION) || peek(Token.Type.TILDE)){
			// unary operator matched.
			Token op = tokens.get();
			return new ExpressionNode(op, parseExpLevel3());
		}else{
			ExpressionNode left = parseExpLevel2();
			ExpressionNode right = null;
			
			Token op = null;
			if(peek(Token.Type.STAR) || peek(Token.Type.FSLASH)
					|| peek(Token.Type.DOUBLEFSLASH) || peek(Token.Type.PERCENT)){
				op = tokens.get();
				right = parseExpLevel3();
			}
			
			if(right != null){
				return new ExpressionNode(op, left, right);
			}else{
				return left;
			}
		}
		
	}
	
	private ExpressionNode parseExpLevel2(){
		// ++ and -- (post)
		ExpressionNode left = parseExpLevel1();
		if(peek(Token.Type.DOUBLEPLUS) || peek(Token.Type.DOUBLEMINUS)){
			return new ExpressionNode(tokens.get(), left);
		}
		return left;
	}
	
	private ExpressionNode parseExpLevel1(){
		// **
		ExpressionNode left = parseExpLevel0();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOUBLESTAR)){
			op = tokens.get();
			right = parseExpLevel1();
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseExpLevel0(){
		// ., [], ()
		ExpressionNode left = parseLiteralIDOrSubExp();
		ExpressionNode right = null;
		
		Token op = null;
		if(peek(Token.Type.DOT)){
			op = tokens.get();
			right = parseExpLevel0();
			return new ExpressionNode(op, left, right);
		}else if(peek(Token.Type.LBRACKET)){
			op = tokens.get();
			right = parseExpLevel12();
			forceNext(Token.Type.RBRACKET);
			return new ExpressionNode(op, left, right);
		}else if(peek(Token.Type.LPAREN)){
			// TODO
		}
		
		if(right != null){
			return new ExpressionNode(op, left, right);
		}else{
			return left;
		}
	}
	
	private ExpressionNode parseLiteralIDOrSubExp(){
		// literal | id | LPAREN expression RPAREN
		Token.Type tokenType = tokens.peek().getType();
		if(tokenType.isLiteral()){
			
		}else if(tokenType == Token.Type.IDENTIFIER){
			
		}else if(tokenType == Token.Type.LPAREN){
			dropNext();
			ExpressionNode node = parseExpLevel12();
			forceNext(Token.Type.RPAREN);
			return node;
		}
		return null;
	}
	
	public ExpressionBlock parseExpressionOld(){
		
		// expression is:
		// id or literal
		// pre unary operator -> expression
		// expression -> post unary operator
		// expression -> binary operator -> expression
		// ( expression )
		
		ExpressionBlock expression = new ExpressionBlock();
		startBlock(expression);
		
		Deque<ExpressionBlock.ExpressionPiece> output = expression.getExpression();
		Stack<Operator> operators = new Stack<Operator>();
		
		
		boolean nextParenIsCall = false;
		
		// loop until the end of the expression is detected
		while(true){
			
			if(peek(Token.Type.IDENTIFIER)){
				output.push(expression.new ExpressionPiece(tokens.get()));
				nextParenIsCall = true;
				
			}else if(tokens.peek().getType().isLiteral()){
				output.push(expression.new ExpressionPiece(tokens.get()));
				nextParenIsCall = true;
				
			}else if(Operator.match(tokens.peek().getType()) != null){
				
				Operator operator = Operator.match(tokens.peek().getType());
					
				while(operators.peek().getPrecedence() >= operator.getPrecedence()){
					output.push(expression.new ExpressionPiece(operators.pop()));
				}
				operators.push(operator);
				nextParenIsCall = false;
			}else if(peek(Token.Type.LPAREN) && !nextParenIsCall){
				operators.push(null);
			}else if(peek(Token.Type.RPAREN)){
				
				while(operators.peek() != null){
					output.push(expression.new ExpressionPiece(operators.pop()));
				}
				operators.pop();
			}else{
				// Input didn't match literal or operator, so the expression's end has been
				// reached. Break from the loop.
				break;
			}
		}
		
		while(!operators.isEmpty()){
			if(operators.peek() == null){
				// Error! Last thing pushed on stack was an open paren
			}
			output.push(expression.new ExpressionPiece(operators.pop()));
		}
		
		// TODO
		endBlock(expression);
		return expression;
	}
	
	public boolean checkStatement(){
		return true;
	}
	
	public Block parseStatement(){
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
	
	private AstNode parseIdOrLiteral(int minPrecedence){
		// get next id or literal
		Token nextToken = peek();
		Token.Type nextType = nextToken.getType();
		
		if(nextType.isLiteral()){
			return new LiteralNode(nextToken);
		}else if(nextToken.getType() == Token.Type.IDENTIFIER){
			return new IdNode(nextToken);
		}else if(nextType == Token.Type.LPAREN){
			forceNext(Token.Type.LPAREN);
			AstNode result = parseExpression(minPrecedence);
			forceNext(Token.Type.RPAREN);
			return result;
		}else{
			syntaxError("Error parsing expression", nextToken,
					new Token.Type[] { Token.Type.IDENTIFIER, Token.Type.BINARYLITERAL, Token.Type.BOOLLITERAL,
							Token.Type.FLOATLITERAL, Token.Type.HEXLITERAL, Token.Type.INTLITERAL,
							Token.Type.OCTLITERAL, Token.Type.STRINGLITERAL });
		}
		return null;
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
	
	private boolean peek(Token.Type... oneOf){
		for(Token.Type type : oneOf){
			if(peek(type)){
				return true;
			}
		}
		return false;
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
