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
import java.util.List;

import chipmunk.compiler.IllegalImportException;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.subparsers.ImportParser;
import chipmunk.compiler.parser.subparsers.VarDecParser;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.SyntaxError;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.util.Require;

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

	private List<AstNode> moduleRoots;
	
	public ChipmunkParser(TokenStream source){
		tokens = source;
		fileName = "";
		moduleRoots = new ArrayList<>();
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
		while(!tokens.peek(TokenType.EOF)){
			moduleRoots.add(parseModule());
		}
	}
	
	public List<AstNode> getModuleRoots(){
		return moduleRoots;
	}
	
	public AstNode parseModule(){
		AstNode module;

		// parse imports, literal assignments, class definitions, method definitions, and module declarations
		tokens.skipNewlinesAndComments();
		
		if(tokens.peek(TokenType.MODULE)){
			tokens.forceNext(TokenType.MODULE);
			
			List<Token> identifiers = new ArrayList<Token>();
			identifiers.add(tokens.getNext(TokenType.IDENTIFIER));
			
			while(tokens.peek(TokenType.DOT)){
				tokens.dropNext();
				if(tokens.peek(TokenType.IDENTIFIER)){
					identifiers.add(tokens.getNext(TokenType.IDENTIFIER));
				}else{
					syntaxError("module", fileName, tokens.peek(), TokenType.IDENTIFIER);
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
			module = Modules.make(moduleName.toString());
		}else{
			module = Modules.make("default");
		}

		tokens.skipNewlinesAndComments();
		
		Token next = tokens.peek();
		TokenType nextType = next.type();
		while(nextType != TokenType.EOF){
			if(checkImport()){
				
				module.addChild(parseImport());
				
			}else if(checkVarDec()){
				
				module.addChild(parseVarDec());
				
			}else if(checkMethodDef()){
				
				AstNode node = parseMethodDef();
				module.addChild(node);
				
			}else if(checkClassDef()){
				
				AstNode node = parseClassDef();
				module.addChild(node);
				
			}else if(tokens.peek(TokenType.MODULE)){
				// Start of next module. Return this module node.
				break;
			}else{
				// Wuh-oh. Couldn't match one of the above cases. Panic!
				Token got = tokens.peek();
				syntaxError("module", fileName,"module start, class or method def, or variable declaration", got);
			}

			tokens.skipNewlinesAndComments();
			
			next = tokens.peek();
			nextType = next.type();
		}

		return module;
	}
	
	public boolean checkClassDef(){
		return checkClassDef(true);
	}
	
	public boolean checkClassDef(boolean allowFinal){
		if(allowFinal){
			return tokens.peek(TokenType.FINAL, TokenType.CLASS) || tokens.peek(TokenType.CLASS);
		}else{
			return tokens.peek(TokenType.CLASS);
		}
	}
	
	public AstNode parseClassDef(){
		tokens.skipNewlines();
		
		AstNode node = new AstNode(NodeType.CLASS, tokens.getNext(TokenType.CLASS));

		Token id = tokens.getNext(TokenType.IDENTIFIER);
		node.setSymbol(new Symbol(id.text()));

		tokens.forceNext(TokenType.LBRACE);
		tokens.skipNewlinesAndComments();
		while(!tokens.peek(TokenType.RBRACE)){
			// parse class body (only variable declarations and method/class definitions allowed)
			tokens.skipNewlinesAndComments();
			
			boolean shared = false;
			if(tokens.dropNext(TokenType.SHARED)){
				shared = true;
			}
			
			Symbol symbol = new Symbol();
			symbol.setShared(shared);
			
			if(checkVarOrTraitDec()){
				AstNode varNode = parseVarOrTraitDec();
				varNode.getSymbol().setShared(shared);
				node.addChild(varNode);
			}else if(checkMethodDef()){
				AstNode methodNode = parseMethodDef();
				methodNode.getSymbol().setShared(shared);
				node.addChild(methodNode);
			}else if(checkClassDef()){
				AstNode classNode = parseClassDef();
				classNode.getSymbol().setShared(shared);
				node.addChild(classNode);
			}else if(tokens.peek(TokenType.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().text()), fileName, tokens.peek(), TokenType.FINAL, TokenType.VAR, TokenType.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);

			tokens.skipNewlines();
			
			if(tokens.peek(TokenType.EOF)){
				syntaxError(String.format("Expected } at %s %d:%d, got EOF", fileName, tokens.peek().line(), tokens.peek().column()), fileName, tokens.peek());
			}
		}
		tokens.forceNext(TokenType.RBRACE);

		return node;
	}
	
	public AstNode parseAnonClassDef(){
		tokens.skipNewlines();
		
		AstNode node = new AstNode(NodeType.CLASS, new Token("class", TokenType.CLASS));
		
		node.setSymbol(new Symbol(""));

		tokens.forceNext(TokenType.LBRACE);
		tokens.skipNewlinesAndComments();
		while(!tokens.peek(TokenType.RBRACE)){
			// parse class body (only variable declarations and method definitions allowed)
			tokens.skipNewlinesAndComments();
			
			boolean shared = false;
			if(tokens.dropNext(TokenType.SHARED)){
				shared = true;
			}
			
			Symbol symbol = new Symbol();
			symbol.setShared(shared);
			
			if(checkVarOrTraitDec()){
				AstNode varNode = parseVarOrTraitDec();
				varNode.getSymbol().setShared(shared);
				node.addChild(varNode);
			}else if(checkMethodDef()){
				AstNode methodNode = parseMethodDef();
				methodNode.getSymbol().setShared(shared);
				node.addChild(methodNode);
			}else if(checkClassDef()){
				AstNode classNode = parseClassDef();
				classNode.getSymbol().setShared(shared);
				node.addChild(classNode);
			}else if(tokens.peek(TokenType.RBRACE)){
				break;
			}else{
				syntaxError(String.format("Error parsing class body: %s", tokens.peek().text()), fileName, tokens.peek(), TokenType.FINAL, TokenType.VAR, TokenType.DEF);
			}
			
			// TODO - symbol search rules
			node.getSymbolTable().setSymbol(symbol);

			tokens.skipNewlines();
			
			if(tokens.peek(TokenType.EOF)){
				syntaxError(String.format("Expected } at %d:%d, got EOF", tokens.peek().line(), tokens.peek().column()), fileName, tokens.peek());
			}
		}
		tokens.forceNext(TokenType.RBRACE);

		return node;
	}
	
	public boolean checkMethodDef(){
		return checkMethodDef(true);
	}
	
	public boolean checkMethodDef(boolean allowFinal){
		if(allowFinal){
			return tokens.peek(TokenType.FINAL, TokenType.DEF) || tokens.peek(TokenType.DEF);
		}else{
			return tokens.peek(TokenType.DEF);
		}
	}
	
	public AstNode parseMethodDef(){
		tokens.skipNewlinesAndComments();

		boolean isFinal = false;
		if(tokens.dropNext(TokenType.FINAL)){
			isFinal = true;
		}

		var paramParser = new VarDecParser(false, true);

		var def = tokens.getNext(TokenType.DEF);
		AstNode node = Methods.make(def, tokens.getNext(TokenType.IDENTIFIER));
		node.getSymbol().setFinal(isFinal);

		tokens.forceNext(TokenType.LPAREN);
		while(paramParser.peek(tokens)){
			AstNode param = paramParser.parse(tokens);
			tokens.dropNext(TokenType.COMMA);
			Methods.addParam(node, param);
		}
		tokens.forceNext(TokenType.RPAREN);
		tokens.skipNewlinesAndComments();


		if(tokens.dropNext(TokenType.COLON)){
			node.setResultTypeName(tokens.getNext(TokenType.IDENTIFIER));
		}

		tokens.skipNewlinesAndComments();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(tokens.peek(TokenType.LBRACE)) {
			parseBlockBody(node);
		}else if(tokens.peek(TokenType.RBRACE) || tokens.peek().type().isKeyword()){
			// Call "chipmunk.lang.unimplementedMethod()"
			// TODO - this should move to the code generator
			AstNode unimplemented = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, node.getTokenIndex(), node.getLineNumber(), 0),
					new AstNode(NodeType.ID, new Token("unimplementedMethod", TokenType.IDENTIFIER)));
			Methods.addToBody(node, unimplemented);
		}else{
			AstNode expression = parseExpression();
			Methods.addToBody(node, expression);
		}

		return node;
	}
	
	public AstNode parseAnonMethodDef(){
		tokens.skipNewlinesAndComments();
		
		AstNode node = Methods.make(
				new Token("def", TokenType.DEF),
				Methods.anonymousName(tokens.peek().line(), tokens.peek().column()));

		var paramParser = new VarDecParser(false, true);

		tokens.forceNext(TokenType.LPAREN);
		while(paramParser.peek(tokens)){
			AstNode param = paramParser.parse(tokens);
			tokens.dropNext(TokenType.COMMA);
			Methods.addParam(node, param);
		}
		tokens.forceNext(TokenType.RPAREN);
		tokens.skipNewlines();

		if(tokens.dropNext(TokenType.COLON)){
			node.setResultTypeName(tokens.getNext(TokenType.IDENTIFIER));
		}

		tokens.skipNewlines();
		
		// If the next token is a block start, parse this as a regular
		// method. Otherwise, treat it as a single expression method
		// with an automatic return.
		if(tokens.peek(TokenType.LBRACE)) {
			parseBlockBody(node);
		}else {
			AstNode expression = parseExpression();
			Methods.addToBody(node, expression);
		}

		return node;
	}
	
	public boolean checkVarDec(){
		return tokens.peek(TokenType.VAR) || tokens.peek(TokenType.FINAL, TokenType.VAR);
	}
	
	public boolean checkVarOrTraitDec() {
		return tokens.peek(TokenType.VAR)
				|| tokens.peek(TokenType.TRAIT)
				|| tokens.peek(TokenType.FINAL, TokenType.VAR)
				|| tokens.peek(TokenType.FINAL, TokenType.TRAIT);
	}
	
	public AstNode parseVarOrTraitDec() {
		return new VarDecParser(true).parse(tokens);
	}
	
	public AstNode parseVarDec(){
		return new VarDecParser().parse(tokens);
	}
	
	public AstNode parseStatement(){
		// Statements are:
		// (a) variable declarations and assignments
		// (b) method definitions
		// (d) block beginnings
		// (e) expressions (including assignments of existing variables)

		tokens.skipNewlinesAndComments();
		
		if(checkVarDec()){
			return parseVarDec();
		}else if(checkMethodDef()){
			return parseMethodDef();
		}
		//else if(checkClassDef()){
		//	return parseClassDef();
		//}
		else if(tokens.peek().type().isKeyword()){
			// parse block or keyword statement
			Token token = tokens.peek();
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
				AstNode returnNode = new AstNode(NodeType.FLOW_CONTROL, token);

				tokens.dropNext();
				
				if(!tokens.peek(TokenType.NEWLINE)){
					returnNode.addChild(parseExpression());
				}

				return returnNode;
			case THROW:
				AstNode throwNode = new AstNode(NodeType.FLOW_CONTROL, token);

				tokens.dropNext();
				throwNode.addChild(parseExpression());

				return throwNode;
			case BREAK:
			case CONTINUE:
				tokens.dropNext();
				
				AstNode node = new AstNode(NodeType.FLOW_CONTROL, token);

				// TODO - I don't think this should be here
				tokens.dropNext();

				return node;
			default:
				syntaxError("Unexpected token", fileName, token, TokenType.IF, TokenType.WHILE, TokenType.FOR, TokenType.TRY,
					TokenType.RETURN, TokenType.THROW, TokenType.BREAK, TokenType.CONTINUE);
			}
		}else if(!tokens.peek(TokenType.EOF)){
			// it's an expression
			return parseExpression();
		}
		return null;
	}
	
	public AstNode parseIfElse(){
		AstNode node = new AstNode(NodeType.IF_ELSE, tokens.peek());
		
		// keep parsing if & else-if branches until something causes this to break
		while(true){
			// Parse if branch
			AstNode ifBranch = parseIfBranch();
			
			node.addChild(ifBranch);

			tokens.skipNewlinesAndComments();
			
			if(tokens.peek(TokenType.ELSE)){
				tokens.skipNewlines();
				var elseToken = tokens.get();
				// it's the else block
				if(tokens.peek(TokenType.LBRACE)){
					AstNode elseBlock = new AstNode(NodeType.ELSE, elseToken);
					
					parseBlockBody(elseBlock);
					node.addChild(elseBlock);
					
					break;
				}
				// if peek() result is not '{', assume it's another if branch
				// and keep going
			}else{
				// it's the end of the if-else chain
				break;
			}
		}

		return node;
	}
	
	public AstNode parseIfBranch(){
		AstNode ifBranch = new AstNode(NodeType.IF, tokens.getNext(TokenType.IF));

		tokens.forceNext(TokenType.LPAREN);
		tokens.skipNewlines();
		
		ifBranch.addChild(parseExpression());

		tokens.skipNewlines();
		tokens.forceNext(TokenType.RPAREN);
		
		parseBlockBody(ifBranch);

		return ifBranch;
	}
	
	public AstNode parseWhile(){

		AstNode node = new AstNode(NodeType.WHILE, tokens.getNext(TokenType.WHILE));
		tokens.forceNext(TokenType.LPAREN);

		tokens.skipNewlines();
		node.addChild(parseExpression());

		tokens.skipNewlines();
		tokens.forceNext(TokenType.RPAREN);
		
		parseBlockBody(node);

		return node;
	}
	
	public AstNode parseFor(){
		AstNode node = new AstNode(NodeType.FOR, tokens.getNext(TokenType.FOR));

		tokens.forceNext(TokenType.LPAREN);

		tokens.skipNewlines();
		tokens.dropNext(TokenType.VAR);

		var varID = tokens.getNext(TokenType.IDENTIFIER);

		AstNode iter = new AstNode(NodeType.ITERATOR, tokens.getNext(TokenType.IN));
		
		AstNode expr = parseExpression();
		
		AstNode decl = VarDec.makeImplicit(varID);

		iter.addChild(decl);
		iter.addChild(expr);

		node.addChild(iter);
		iter.setSymbol(new Symbol(varID + "$it"));

		tokens.skipNewlines();
		tokens.forceNext(TokenType.RPAREN);
		
		parseBlockBody(node);

		return node;
	}
	
	public void parseBlockBody(AstNode node){
		tokens.skipNewlines();
		tokens.forceNext(TokenType.LBRACE);
		
		while(!tokens.peek(TokenType.RBRACE)){
			tokens.skipNewlinesAndComments();
			if(tokens.peek(TokenType.RBRACE)){
				break;
			}
			node.addChild(parseStatement());
			tokens.skipNewlinesAndComments();
			
			if(tokens.peek(TokenType.EOF)){
				syntaxError("block body", fileName, tokens.peek(), TokenType.RBRACE);
			}
		}

		tokens.forceNext(TokenType.RBRACE);
	}
	
	public AstNode parseTryCatch(){
		AstNode node = new AstNode(NodeType.TRY_CATCH);

		AstNode tryNode = new AstNode(NodeType.TRY, tokens.getNext(TokenType.TRY));
		parseBlockBody(tryNode);

		node.addChild(tryNode);
		
		// parse and add catch blocks
		//while(true){
			// TODO - support finally blocks and typed multi-catch
		AstNode catchNode = new AstNode(NodeType.CATCH, tokens.getNext(TokenType.CATCH));
		tokens.forceNext(TokenType.LPAREN);
			
		// if catch block has type of exception, include in node
		// TODO - this is wrong
		//if(tokens.peek(TokenType.IDENTIFIER, TokenType.IDENTIFIER)){
			//catchNode.addChild(new IdNode(tokens.getNext(TokenType.IDENTIFIER)));
		//}

		var id = tokens.getNext(TokenType.IDENTIFIER);
		var idNode = new AstNode(NodeType.ID, id);
		idNode.setSymbol(new Symbol(id.text()));

		var dec = new AstNode(NodeType.VAR_DEC, id).withChild(idNode);
		catchNode.addChild(dec);
		tokens.forceNext(TokenType.RPAREN);
			
		parseBlockBody(catchNode);

		node.addChild(catchNode);
			
			//if(!peek(Token.Type.CATCH)){
			//	break;
			//}
		//}
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
	public AstNode parseImport(){
		tokens.skipNewlinesAndComments();

		var parser = new ImportParser();
		var node = parser.parse(tokens);
		if(Imports.isAliased(node)){
			Require.require(!Imports.isImportAll(node), IllegalImportException::new, "Cannot alias a * import: %s", node);
			Require.require(Imports.symbols(node).size() == Imports.aliases(node).size(), IllegalImportException::new,
					"Imports must have the same number of aliases as imported symbols");
		}
		Require.require(Imports.symbols(node).size() > 0, IllegalImportException::new,"Must import symbols from module");
		if(Imports.symbols(node).size() > 1){
			Require.require(Imports.symbols(node).stream().noneMatch(s -> s.getName().equals("*")), IllegalImportException::new,
					"Cannot import * along with other symbols");
		}
		return node;
	}
	
	/**
	 * Parse expressions with precedence climbing algorithm
	 * @return AST of the expression
	 */
	public AstNode parseExpression(){
		return new ExpressionParser(tokens).parseExpression();
	}

	
	public static void syntaxError(String langElement, String fileName, Token got, TokenType... expected) throws SyntaxError {
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
				langElement, fileName, got.line(), got.column(), expectedTypes.toString(), got);
		
		SyntaxError error = new SyntaxError(msg);
		error.setExpected(expected);
		error.setGot(got);
		throw error;
	}
	
	public static void syntaxError(String langElement, String fileName, String expected, Token got) throws SyntaxError {
		String msg = String.format("Error parsing %s at %s %d:%d: expected %s, got %s",
				langElement, fileName, got.line(), got.column(), expected, got);
		SyntaxError error = new SyntaxError(msg);
		error.setExpected(new TokenType[]{});
		error.setGot(got);
		throw error;
	}
}
