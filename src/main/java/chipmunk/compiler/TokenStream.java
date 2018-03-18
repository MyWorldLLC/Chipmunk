package chipmunk.compiler;

import java.util.ArrayList;
import java.util.List;

public class TokenStream {

	protected List<Token> tokens;
	protected int cursor;
	
	public TokenStream(){
		tokens = new ArrayList<Token>();
		cursor = 0;
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
	
	public Token peek(int count){
		
		int index = cursor + count;
		
		if(index >= tokens.size()){
			index = tokens.size() - 1;
		}
		
		return tokens.get(index);
	}
	
	public Token get(){
		
		Token token = tokens.get(cursor);
		cursor++;
		
		return token;
	}
	
	public Token[] get(int count){
		
		Token[] getTokens = new Token[count];
		
		for(int i = 0; i < count && cursor < tokens.size(); i++, cursor++){
			getTokens[i] = tokens.get(i);
		}
		
		return getTokens;
	}
	
	public int remaining(){
		return tokens.size() - cursor;
	}
	
	public void reset(){
		cursor = 0;
	}
	
	public void rewind(int places){
		
		cursor -= places;
		
		if(cursor < 0){
			cursor = 0;
		}
		
	}
	
	public void skip(int places){
		
		cursor += places;
		
		if(cursor >= tokens.size()){
			cursor = tokens.size() - 1;
		}
		
	}
	
	public String toString(){
		return tokenDump(cursor);
	}
	
	public String tokenDump(int startAt){
		StringBuilder builder = new StringBuilder();
		for(int i = startAt; i < tokens.size(); i++){
			builder.append(tokens.get(i).getText());
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
			builder.append(tokens.get(i).getType().name().toLowerCase());
			if(i < tokens.size() - 1){
				builder.append(' ');
			}
		}
		
		return builder.toString();
	}
}
