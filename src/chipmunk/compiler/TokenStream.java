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
}
