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

package chipmunk.compiler.lexer;

import chipmunk.compiler.parser.ChipmunkParser;
import chipmunk.util.SeekableSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TokenStream implements SeekableSequence<Token> {

	protected final String fileName;
	protected final List<Token> tokens;
	protected int cursor;
	protected int mark;
	
	public TokenStream(){
		this(null);
	}

	public TokenStream(String fileName){
		this.fileName = fileName;
		tokens = new ArrayList<>();
		cursor = 0;
		mark = 0;
	}

	private TokenStream(String fileName, List<Token> tokens, int cursor, int mark){
		this.fileName = fileName;
		this.tokens = tokens;
		this.cursor = cursor;
		this.mark = mark;
	}

	@Override
	public TokenStream duplicate(){
		return new TokenStream(fileName, Collections.unmodifiableList(tokens), cursor, mark);
	}

	public String getFileName(){
		return fileName;
	}

	public int getStreamPosition(){
		return cursor;
	}
	
	public void append(Token token){
		tokens.add(token);
	}
	
	public Token peek(){
		return tokens.get(cursor);
	}

	@Override
	public boolean hasMore() {
		return remaining() > 0;
	}

	public Token peek(int count){
		int index = Math.min(Math.max(cursor + count, 0), tokens.size() - 1);
		return tokens.get(index);
	}
	
	public Token get(){
		
		Token token = tokens.get(cursor);
		cursor = Math.min(cursor + 1, tokens.size() - 1);
		
		return token;
	}
	
	public Token get(int skip){
		return skip(skip).get();
	}

	public Token getNext(TokenType type){
		Token token = get();

		if(token.type() != type){
			ChipmunkParser.syntaxError("", "", token, type);
		}

		return token;
	}

	public void forceNext(TokenType type){
		Token token = get();

		if(token.type() != type){
			ChipmunkParser.syntaxError("", "", token, type);
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
			get();
			return true;
		}
		return false;
	}

	public void dropNext(){
		get();
	}

	public boolean peek(TokenType type){
		Token token = peek();
		return token.type() == type;
	}

	public boolean peek(int places, TokenType type){
		return peek(places).type() == type;
	}

	public boolean peek(TokenType... types){
		for(int i = 0; i < types.length; i++){
			if(peek(i).type() != types[i]){
				return false;
			}
		}
		return true;
	}

	public int remaining(){
		return tokens.size() - cursor;
	}
	
	public void reset(){
		cursor = 0;
		mark = 0;
	}

	public int mark(){
		mark = cursor;
		return mark;
	}

	public void rewind(){
		cursor = mark;
	}
	
	public void rewind(int places){
		cursor = Math.max(cursor - places, 0);
	}
	
	public TokenStream skip(int places){
		cursor = Math.min(cursor + places, tokens.size() - 1);
		return this;
	}

	public TokenStream seek(int index){
		cursor = Math.min(Math.max(index, 0), tokens.size() - 1);
		return this;
	}
	
	public String toString(){
		return tokenDump(cursor);
	}
	
	public String tokenDump(int startAt){
		StringBuilder builder = new StringBuilder();
		for(int i = startAt; i < tokens.size(); i++){
			builder.append(tokens.get(i).text());
			if(i < tokens.size() - 1){
				builder.append(' ');
			}
		}
		
		return builder.toString();
	}
	
	public String tokenDump(){
		return tokenDump(0);
	}
	
	public String tokenTypeDump(){
		return tokenTypeDump(0);
	}
	
	public String tokenTypeDump(int startAt){
		StringBuilder builder = new StringBuilder();
		for(int i = startAt; i < tokens.size(); i++){
			builder.append(tokens.get(i).type().name().toLowerCase());
			if(i < tokens.size() - 1){
				builder.append(' ');
			}
		}
		
		return builder.toString();
	}
}
