/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.compiler.IllegalImportException;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.SyntaxError;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.parser.parselets.*;

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
	
	private Map<TokenType, InfixParselet> infix;
	private Map<TokenType, PrefixParselet> prefix;
	
	private List<ModuleNode> moduleRoots;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
		fileName = "";
		moduleRoots = new ArrayList<ModuleNode>();
		
		infix = new HashMap<TokenType, InfixParselet>();
		prefix = new HashMap<TokenType, PrefixParselet>();
		
		// register parselets
		
		// identifiers and literals
		register(TokenType.IDENTIFIER, new NameParselet());
		register(TokenType.BOOLLITERAL, new LiteralParselet());
		register(TokenType.BINARYLITERAL, new LiteralParselet());
		register(TokenType.HEXLITERAL, new LiteralParselet());
		register(TokenType.OCTLITERAL, new LiteralParselet());
		register(TokenType.INTLITERAL, new LiteralParselet());
		register(TokenType.FLOATLITERAL, new LiteralParselet());
		register(TokenType.STRINGLITERAL, new LiteralParselet());
		register(TokenType.NULL, new LiteralParselet());
		register(TokenType.LBRACKET, new ListParselet());
		register(TokenType.LBRACE, new MapParselet());
		
		// prefix operators
		prefixOp(TokenType.PLUS);
		prefixOp(TokenType.MINUS);
		prefixOp(TokenType.DOUBLEPLUS);
		prefixOp(TokenType.DOUBLEMINUS);
		prefixOp(TokenType.EXCLAMATION);
		prefixOp(TokenType.TILDE);
		
		// parentheses for grouping in expressions
		register(TokenType.LPAREN, new GroupingParselet());
		
		// binary infix operators
		register(TokenType.PLUS, new AddSubOperatorParselet());
		register(TokenType.MINUS, new AddSubOperatorParselet());
		register(TokenType.STAR, new MulDivOperatorParselet());
		register(TokenType.FSLASH, new MulDivOperatorParselet());
		register(TokenType.DOUBLEFSLASH, new MulDivOperatorParselet());
		register(TokenType.PERCENT, new MulDivOperatorParselet());
		
		register(TokenType.DOUBLESTAR, new PowerOperatorParselet());
		
		register(TokenType.DOT, new DotOperatorParselet());
		register(TokenType.AS, new CastOperatorParselet());
		
		register(TokenType.DOUBLELESSTHAN, new ShiftRangeOperatorParselet());
		register(TokenType.DOUBLEMORETHAN, new ShiftRangeOperatorParselet());
		register(TokenType.DOUBLEDOTLESS, new ShiftRangeOperatorParselet());
		register(TokenType.DOUBLEDOT, new ShiftRangeOperatorParselet());
		
		register(TokenType.LESSTHAN, new LesserGreaterInstanceOfOperatorParselet());
		register(TokenType.LESSEQUALS, new LesserGreaterInstanceOfOperatorParselet());
		register(TokenType.MORETHAN, new LesserGreaterInstanceOfOperatorParselet());
		register(TokenType.MOREEQUALS, new LesserGreaterInstanceOfOperatorParselet());
		register(TokenType.INSTANCEOF, new LesserGreaterInstanceOfOperatorParselet());

		register(TokenType.IS, new EqualityOperatorParselet());
		register(TokenType.DOUBLEEQUAlS, new EqualityOperatorParselet());
		register(TokenType.EXCLAMATIONEQUALS, new EqualityOperatorParselet());
		
		register(TokenType.AMPERSAND, new BitAndOperatorParselet());
		register(TokenType.BAR, new BitOrOperatorParselet());
		register(TokenType.CARET, new BitXOrOperatorParselet());
		
		register(TokenType.DOUBLEAMPERSAND, new AndOperatorParselet());
		register(TokenType.DOUBLEBAR, new OrOperatorParselet());
		register(TokenType.DOUBLECOLON, new BindingOperatorParselet());
		
		register(TokenType.EQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEPLUSEQUALS, new AssignOperatorParselet());
		register(TokenType.PLUSEQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEMINUSEQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLESTAREQUALS, new AssignOperatorParselet());
		register(TokenType.STAREQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEFSLASHEQUALS, new AssignOperatorParselet());
		register(TokenType.FSLASHEQUALS, new AssignOperatorParselet());
		register(TokenType.PERCENTEQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEAMPERSANDEQUALS, new AssignOperatorParselet());
		register(TokenType.AMPERSANDEQUALS, new AssignOperatorParselet());
		register(TokenType.CARETEQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEBAREQUALS, new AssignOperatorParselet());
		register(TokenType.BAREQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLELESSEQUALS, new AssignOperatorParselet());
		register(TokenType.TRIPLEMOREQUALS, new AssignOperatorParselet());
		register(TokenType.DOUBLEMOREEQUALS, new AssignOperatorParselet());
		register(TokenType.TILDEEQUALS, new AssignOperatorParselet());
		
		// postfix operators
		register(TokenType.DOUBLEPLUS, new PostIncDecParselet());
		register(TokenType.DOUBLEMINUS, new PostIncDecParselet());
		register(TokenType.LPAREN, new CallOperatorParselet());
		register(TokenType.LBRACKET, new IndexOperatorParselet());
		
		// method def operator (allow method definitions in expressions)
		register(TokenType.DEF, new MethodDefParselet());
		// class definition operator (allows creating anonymous classes in expressions)
		//register(Token.Type.CLASS, new ClassDefParselet());
	}
	
	protected void register(TokenType type, InfixParselet parselet){
		infix.put(type, parselet);
	}
	
	protected void register(TokenType type, PrefixParselet parselet){
		prefix.put(type, parselet);
	}
	
	protected void prefixOp(TokenType op){
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
		while(!peek(TokenType.EOF)){
			moduleRoots.add(parseModule());
		}
	}
	
	public List<ModuleNode> getModuleRoots(){
		return moduleRoots;
	}
	
	public ModuleNode parseModule(){
		ModuleNode module = new ModuleNode();
		module.setFileName(fileName);
		startNode(module);
		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		skipNewlinesAndComments();
		
		if(peek(TokenType.MODULE)){
			forceNext(TokenType.MODULE);
			
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(TokenType.IDENTIFIER));
			
			while(peek(TokenType.DOT)){
				dropNext();
				if(peek(TokenType.IDENTIFIER)){
					identifiers.add(getNext(TokenType.IDENTIFIER));
				}else{
					syntaxError("module", tokens.peek(), TokenType.IDENTIFIER);
				}
			}
			
			// piece module name back together
			StringBuilder moduleName = new StringBuilder();
			if(identifiers.size() == 1){
				moduleName.append(identifiers.get(0).text());
			}else{
				for(int i = 0; i < identifiers.size(); i++){
					moduleName.append(identifiers.get(i).text());
					if (i < identifiers.size() - 1) {
						moduleName.append('.');
					}
				}
			}
			module.setName(moduleName.toString());
		}else{
			module.setName("default");
		}
		
		skipNewlinesAndComments();
		
		Token next = tokens.peek();
		TokenType nextType = next.type();
		while(nextType != TokenType.EOF){
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
				
			}else if(peek(TokenType.MODULE)){
				// Start of next module. Return this module node.
				break;
			}else{
				// Wuh-oh. Couldn't match one of the above cases. Panic!
				Token got = peek();
				syntaxError("module", "module start, class or method def, or variable declaration", got);
			}
			
			skipNewlinesAndComments();
			
			next = tokens.peek();
			nextType = next.type();
		}
		endNode(module);
		return module;
	}
	
	public boolean checkClassDef(){
		return checkClassDef(true);
	}
	
	public boolean checkClassDef(boolean allowFinal){
		if(allowFinal){
			return peek(TokenType.FINAL, TokenType.CLASS) || peek(TokenType.CLASS);
		}else{
			return peek(TokenType.CLASS);
		}
	}
	
	public ClassNode parseClassDef(){
		skipNewlines();
		
		ClassNode node = new ClassNode();
		startNode(node);
		
		forceNext(TokenType.CLASS);
		Token id = getNext(TokenType.IDENTIFIER);
		
		node.setName(id.text());
		
		forceNext(TokenType.LBRACE);
		skipNewlinesAndComments();
		while(!peek(TokenType.RBRACE)){
			// parse class body (only variable declarations and method/class definitions allowed)
			skipNewlinesAndComments();
			
			boolean shared = false;
			if(dropNext(TokenType.SHARED)){
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
			}else if(peek(TokenType.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().text()), tokens.peek(), TokenType.FINAL, TokenType.VAR, TokenType.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);
			
			skipNewlines();
			
			if(peek(TokenType.EOF)){
				syntaxError(String.format("Expected } at %d:%d, got EOF",peek().line(), peek().column()), peek());
			}
		}
		forceNext(TokenType.RBRACE);
		endNode(node);
		return node;
	}
	
	public ClassNode parseAnonClassDef(){
		skipNewlines();
		
		ClassNode node = new ClassNode();
		startNode(node);
		
		node.setName("");
		
		forceNext(TokenType.LBRACE);
		skipNewlinesAndComments();
		while(!peek(TokenType.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			skipNewlinesAndComments();
			
			boolean shared = false;
			if(dropNext(TokenType.SHARED)){
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
			}else if(peek(TokenType.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().text()), tokens.peek(), TokenType.FINAL, TokenType.VAR, TokenType.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);
			
			skipNewlines();
			
			if(peek(TokenType.EOF)){
				syntaxError(String.format("Expected } at %d:%d, got EOF",peek().line(), peek().column()), peek());
			}
		}
		forceNext(TokenType.RBRACE);
		endNode(node);
		return node;
	}
	
	public boolean checkMethodDef(){
		return checkMethodDef(true);
	}
	
	public boolean checkMethodDef(boolean allowFinal){
		if(allowFinal){
			return peek(TokenType.FINAL, TokenType.DEF) || peek(TokenType.DEF);
		}else{
			return peek(TokenType.DEF);
		}
	}
	
	public MethodNode parseMethodDef(){
		skipNewlinesAndComments();
		
		MethodNode node = new MethodNode();
		startNode(node);
		
		if(dropNext(TokenType.FINAL)){
			node.getSymbol().setFinal(true);
		}
		
		forceNext(TokenType.DEF);
		node.setName(getNext(TokenType.IDENTIFIER).text());
		
		forceNext(TokenType.LPAREN);
		while(peek(TokenType.IDENTIFIER)){
			VarDecNode param = new VarDecNode();
			param.setVar(new IdNode(getNext(TokenType.IDENTIFIER)));
			if(peek(TokenType.EQUALS)){
				dropNext();
				param.setAssignExpr(parseExpression());
			}
			dropNext(TokenType.COMMA);
			node.addParam(param);
		}
		forceNext(TokenType.RPAREN);
		skipNewlines();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(peek(TokenType.LBRACE)) {
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
		node.setName("anonL" + peek().line() + "C" + peek().column());
		
		forceNext(TokenType.LPAREN);
		while(peek(TokenType.IDENTIFIER)){
			VarDecNode param = new VarDecNode();
			param.setVar(new IdNode(getNext(TokenType.IDENTIFIER)));
			if(peek(TokenType.EQUALS)){
				dropNext();
				param.setAssignExpr(parseExpression());
			}
			dropNext(TokenType.COMMA);
			node.addParam(param);
			dropNext(TokenType.COMMA);
		}
		forceNext(TokenType.RPAREN);
		skipNewlines();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(peek(TokenType.LBRACE)) {
			parseBlockBody(node);
		}else {
			AstNode expression = parseExpression();
			node.addToBody(expression);
		}
		
		endNode(node);
		return node;
	}
	
	public boolean checkVarDec(){
		return peek(TokenType.VAR) || peek(TokenType.FINAL, TokenType.VAR);
	}
	
	public boolean checkVarOrTraitDec() {
		return peek(TokenType.VAR)
				|| peek(TokenType.TRAIT)
				|| peek(TokenType.FINAL, TokenType.VAR)
				|| peek(TokenType.FINAL, TokenType.TRAIT);
	}
	
	public VarDecNode parseVarOrTraitDec() {
		
		VarDecNode dec = new VarDecNode();
		startNode(dec);
		if(dropNext(TokenType.FINAL)){
			dec.getSymbol().setFinal(true);
		}
		
		if(peek(TokenType.TRAIT)) {
			dropNext();
			dec.getSymbol().setTrait(true);
		}else {
			forceNext(TokenType.VAR);
		}
		Token id = getNext(TokenType.IDENTIFIER);
		IdNode idNode = new IdNode(id);
		idNode.setLineNumber(id.line());
		dec.setVar(idNode);
		
		
		if(peek(TokenType.EQUALS)){
			forceNext(TokenType.EQUALS);
			dec.setAssignExpr(parseExpression());
		}
		
		endNode(dec);
		return dec;
	}
	
	public VarDecNode parseVarDec(){
		VarDecNode dec = new VarDecNode();
		startNode(dec);
		if(dropNext(TokenType.FINAL)){
			dec.getSymbol().setFinal(true);
		}
		
		forceNext(TokenType.VAR);
		Token id = getNext(TokenType.IDENTIFIER);
		
		dec.setVar(new IdNode(id));
		
		if(peek(TokenType.EQUALS)){
			forceNext(TokenType.EQUALS);
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
		else if(peek().type().isKeyword()){
			// parse block or keyword statement
			Token token = peek();
			TokenType type = token.type();
			
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
				
				if(!peek(TokenType.NEWLINE)){
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
				syntaxError("Unexpected token", token, TokenType.IF, TokenType.WHILE, TokenType.FOR, TokenType.TRY,
					TokenType.RETURN, TokenType.THROW, TokenType.BREAK, TokenType.CONTINUE);
			}
		}else if(!peek(TokenType.EOF)){
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
			
			if(dropNext(TokenType.ELSE)){
				skipNewlines();
				// it's the else block
				if(peek(TokenType.LBRACE)){
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
		
		forceNext(TokenType.IF);
		forceNext(TokenType.LPAREN);
		skipNewlines();
		
		ifBranch.setGuard(parseExpression());
		
		skipNewlines();
		forceNext(TokenType.RPAREN);
		
		parseBlockBody(ifBranch);
		
		endNode(ifBranch);
		return ifBranch;
	}
	
	public WhileNode parseWhile(){
		WhileNode node = new WhileNode();
		startNode(node);
		
		forceNext(TokenType.WHILE);
		forceNext(TokenType.LPAREN);
		
		skipNewlines();
		node.setGuard(parseExpression());
		
		skipNewlines();
		forceNext(TokenType.RPAREN);
		
		parseBlockBody(node);
		
		endNode(node);
		return node;
	}
	
	public ForNode parseFor(){
		ForNode node = new ForNode();
		startNode(node);
		
		forceNext(TokenType.FOR);
		forceNext(TokenType.LPAREN);
		
		skipNewlines();
		dropNext(TokenType.VAR);

		IteratorNode iter = new IteratorNode();
		startNode(iter);
		
		IdNode varID = new IdNode(getNext(TokenType.IDENTIFIER));
		varID.setLineNumber(varID.getID().line());
		varID.setBeginTokenIndex(varID.getID().index());
		varID.setEndTokenIndex(varID.getID().index() + 1);

		forceNext(TokenType.IN);
		
		AstNode expr = parseExpression();
		
		VarDecNode decl = new VarDecNode(varID);
		decl.setLineNumber(varID.getLineNumber());
		decl.setBeginTokenIndex(varID.getID().index());
		decl.setEndTokenIndex(varID.getID().index() + 1);

		iter.setID(decl);
		iter.setIter(expr);

		endNode(iter);

		node.setIterator(iter);
		
		skipNewlines();
		forceNext(TokenType.RPAREN);
		
		parseBlockBody(node);
		
		endNode(node);
		return node;
	}
	
	public void parseBlockBody(BlockNode node){
		skipNewlines();
		forceNext(TokenType.LBRACE);
		
		while(!peek(TokenType.RBRACE)){
			skipNewlinesAndComments();
			if(peek(TokenType.RBRACE)){
				break;
			}
			node.addToBody(parseStatement());
			skipNewlinesAndComments();
			
			if(peek(TokenType.EOF)){
				syntaxError("block body", peek(), TokenType.RBRACE);
			}
		}

		forceNext(TokenType.RBRACE);
	}
	
	public AstNode parseTryCatch(){
		TryCatchNode node = new TryCatchNode();
		startNode(node);
		
		forceNext(TokenType.TRY);
		TryNode tryNode = new TryNode();
		
		startNode(tryNode);
		parseBlockBody(tryNode);
		endNode(tryNode);
		// add try block to body
		node.setTryBlock(tryNode);
		
		// parse and add catch blocks
		//while(true){
			// TODO - support finally blocks and typed multi-catch
		forceNext(TokenType.CATCH);
		forceNext(TokenType.LPAREN);
			
		CatchNode catchNode = new CatchNode();
		startNode(catchNode);
			
		// if catch block has type of exception, include in node
		if(peek(TokenType.IDENTIFIER, TokenType.IDENTIFIER)){
			catchNode.setExceptionType(getNext(TokenType.IDENTIFIER));
		}
			
		catchNode.setExceptionName(getNext(TokenType.IDENTIFIER));
		forceNext(TokenType.RPAREN);
			
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
		TokenType nextType = tokens.peek().type();
		return nextType == TokenType.FROM || nextType == TokenType.IMPORT;
	}
	
	/**
	 * Consumes the next import statement from the token stream.
	 * @return the import block for the statement
	 */
	public ImportNode parseImport(){
		skipNewlines();
		ImportNode node = new ImportNode();
		startNode(node);
		
		if(peek(TokenType.IMPORT)){
			dropNext(TokenType.IMPORT);
			// import single symbol
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(TokenType.IDENTIFIER));
			
			while(peek(TokenType.DOT)){
				dropNext();
				if(peek(TokenType.IDENTIFIER)){
					identifiers.add(getNext(TokenType.IDENTIFIER));
				}else if(peek(TokenType.STAR)){
					identifiers.add(getNext(TokenType.STAR));
					node.setImportAll(true);
					break;
				}else{
					throw new IllegalImportException("Expected identifier or *, got " + tokens.peek().text());
				}
			}
			
			// piece module name back together
			StringBuilder moduleName = new StringBuilder();
			if(identifiers.size() == 1){
				moduleName.append(identifiers.get(0).text());
			}else{
				for(int i = 0; i < identifiers.size() - 1; i++){
					moduleName.append(identifiers.get(i).text());
					if (i < identifiers.size() - 2) {
						moduleName.append('.');
					}
				}
			}
			
			if(identifiers.size() > 1){
				node.addSymbol(identifiers.get(identifiers.size() - 1).text());
			}
			
			node.setModule(moduleName.toString());
		}else if(peek(TokenType.FROM)){
			dropNext(TokenType.FROM);
			
			// import multiple symbols
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(getNext(TokenType.IDENTIFIER));
			
			while(peek(TokenType.DOT)){
				dropNext(TokenType.DOT);
				if(peek(TokenType.IDENTIFIER)){
					identifiers.add(getNext(TokenType.IDENTIFIER));
				}else if(peek(TokenType.STAR)){
					identifiers.add(getNext(TokenType.STAR));
					node.setImportAll(true);
				}else{
					throw new IllegalImportException("Expected identifier or *, got " + tokens.peek().text());
				}
			}
			
			StringBuilder moduleName = new StringBuilder();
			for(int i = 0; i < identifiers.size(); i++){
				moduleName.append(identifiers.get(i).text());
				if(i < identifiers.size() - 1){
					moduleName.append('.');
				}
			}
			
			node.setModule(moduleName.toString());
			
			forceNext(TokenType.IMPORT);
			
			if(peek(TokenType.IDENTIFIER)){
				node.addSymbol(getNext(TokenType.IDENTIFIER).text());
				while(peek(TokenType.COMMA)){
					dropNext(TokenType.COMMA);
					node.addSymbol(getNext(TokenType.IDENTIFIER).text());
				}
			}else{
				forceNext(TokenType.STAR);
				node.setImportAll(true);
				node.addSymbol("*");
			}
			
		}else{
			syntaxError("Invalid import", tokens.get(), TokenType.IMPORT, TokenType.FROM);
		}
		
		if(peek(TokenType.AS)){
			
			if(node.getSymbols().contains("*")){
				throw new IllegalImportException("Cannot alias a * import");
			}
			
			dropNext();
			// parse aliases
			node.addAlias(getNext(TokenType.IDENTIFIER).text());
			
			while(peek(TokenType.COMMA)){
				dropNext(TokenType.COMMA);
				node.addAlias(getNext(TokenType.IDENTIFIER).text());
			}
			
			if(node.getSymbols().size() < node.getAliases().size()){
				throw new IllegalImportException("Cannot have more aliases than imported symbols");
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
		
		PrefixParselet prefixParser = prefix.get(token.type());
		
		if(prefixParser == null){
			syntaxError("expression", "literal, id, or unary operator", token);
		}
		
		AstNode left = prefixParser.parse(this, token);
		left.setLineNumber(token.line());
		
		token = tokens.peek();
		while(minPrecedence < getPrecedence(token)){
			token = tokens.get();
			
			InfixParselet infixParser = infix.get(token.type());
			
			if(infixParser == null){
				syntaxError("expression", "literal, id, or binary operator", token);
			}
			
			left = infixParser.parse(this, left, token);
			left.setLineNumber(token.line());
			token = tokens.peek();
		}
		
		return left;
	}
	
	private int getPrecedence(Token token){
		InfixParselet parselet = infix.get(token.type());
		if(parselet != null){
			return parselet.getPrecedence();
		}else{
			return 0;
		}
	}
	
	public Token getNext(TokenType type){
		Token token = tokens.get();
		
		if(token.type() != type){
			syntaxError("", token, type);
		}
		
		return token;
	}
	
	public void forceNext(TokenType type){
		Token token = tokens.get();
		
		if(token.type() != type){
			syntaxError("", token, type);
		}
	}
	
	public void skipNewlines(){
		while(dropNext(TokenType.NEWLINE)){}
	}
	
	public void skipNewlinesAndComments(){
		while(dropNext(TokenType.NEWLINE) || dropNext(TokenType.COMMENT)){}
	}
	
	public boolean dropNext(TokenType type){
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
	
	public boolean peek(TokenType type){
		Token token = tokens.peek();
		
		return token.type() == type;
	}
	
	public boolean peek(int places, TokenType type){
		return tokens.peek(places).type() == type;
	}
	
	public boolean peek(TokenType... types){
		for(int i = 0; i < types.length; i++){
			if(peek(i).type() != types[i]){
				return false;
			}
		}
		return true;
	}
	
	private void startNode(AstNode node){
		node.setBeginTokenIndex(tokens.getStreamPosition());
		node.setLineNumber(tokens.peek().line());
	}
	
	private void endNode(AstNode node){
		node.setEndTokenIndex(tokens.getStreamPosition());
	}
	
	public void syntaxError(String context, Token got, TokenType... expected) throws SyntaxError {
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
				context, fileName, got.line(), got.column(), expectedTypes.toString(), got.text());
		
		SyntaxError error = new SyntaxError(msg);
		error.setExpected(expected);
		error.setGot(got);
		throw error;
	}
	
	public void syntaxError(String context, String expected, Token got) throws SyntaxError {
		String msg = String.format("Error parsing %s at %s %d:%d: expected %s, got %s",
				context, fileName, got.line(), got.column(), expected, got.text());
		SyntaxError error = new SyntaxError(msg);
		error.setExpected(new TokenType[]{});
		error.setGot(got);
		throw error;
	}
}
