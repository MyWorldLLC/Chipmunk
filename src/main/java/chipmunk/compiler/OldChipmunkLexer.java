package chipmunk.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OldChipmunkLexer {
	
	protected TokenStream stream;
	protected Map<Token.Type, Matcher> matchers;
	protected CharSequence src;
	
	private int line;
	private int column;
	private int cursor;

	public TokenStream lex(CharSequence source){
		
		stream = new TokenStream();
		
		src = source;
		
		line = 1;
		column = 1;
		cursor = 0;
		
		// fill matcher cache
		matchers = new HashMap<Token.Type, Matcher>();
		
		Token.Type[] tokenTypes = Token.Type.values();
		for(int i = 0; i < tokenTypes.length; i++){
			Token.Type type = tokenTypes[i];
			matchers.put(type, type.getPattern().matcher(src));
		}
		
		// spaces
		Pattern spacePattern = Pattern.compile(" ");
		Matcher space = spacePattern.matcher(src);
		
		// tabs
		Pattern tabPattern = Pattern.compile("\\t");
		Matcher tab = tabPattern.matcher(src);
		
		// any whitespace
		Pattern wsPattern = Pattern.compile("\\s");
		Matcher ws = wsPattern.matcher(src);
		
		
		while(cursor < src.length()){
			
			// check for spaces
			space.region(cursor, src.length() - 1);
			if(space.lookingAt()){
				cursor += 1;
				column += 1;
				continue;
			}
			
			// check for tabs
			tab.region(cursor, src.length() - 1);
			if(tab.lookingAt()){
				cursor += 1;
				column += 4;
				continue;
			}
			
			boolean foundToken = false;
			
			// try to match keywords first - this ensures that keywords (with trailing whitespace)
			// are not matched as identifiers because of the order of evaluation of token expressions.
			for(int i = 0; i < tokenTypes.length; i++){
				
				Token.Type type = tokenTypes[i];
				if(type.isKeyword()){
					
					Matcher matcher = matchers.get(type);
					matcher.region(cursor, src.length() - 1);
					
					if(matcher.lookingAt() && !matcher.hitEnd()){
						
						ws.region(matcher.end(), src.length() - 1);

						if(ws.lookingAt()){
							
							String subStr = src.subSequence(cursor, matcher.end()).toString();
							stream.append(new Token(subStr, type, line, column));
							
							cursor += matcher.end();
							column += matcher.end();
							
							foundToken = true;
							break;
						}
						
					}
				}
			}
			
			// try to match non-keyword tokens
			for (int i = 0; (i < tokenTypes.length) && !foundToken; i++) {

				Token.Type type = tokenTypes[i];

				if (!type.isKeyword()) {

					Matcher matcher = matchers.get(type);
					matcher.region(cursor, src.length() - 1);

					if (matcher.lookingAt()) {
						
						// float literals may be detected as int.int if the integer literal
						// token type is checked before the float literal token type is checked.
						// This ensures that any time an int literal is matched that it really is
						// an int literal and not a float literal
						if(type == Token.Type.INTLITERAL){
							
							Matcher floatMatcher = matchers.get(Token.Type.FLOATLITERAL);
							floatMatcher.region(cursor, src.length() - 1);
							
							if(floatMatcher.lookingAt()){
								// if we really have a float, change token type
								// and matcher
								type = Token.Type.FLOATLITERAL;
								matcher = floatMatcher;
								
							}
						}

						String subStr = src.subSequence(cursor, matcher.end()).toString();
						stream.append(new Token(subStr, type, line, column));

						cursor += matcher.end();
						
						if (type == Token.Type.NEWLINE) {
							column = 1;
							line += 1;
						} else {
							column += matcher.end();
						}
						
						foundToken = true;
						break;
					
					}
				}
			}
			
			// error - we couldn't match a valid token
			if(!foundToken){
				throw new SyntaxErrorChipmunk("Syntax error at line " + line + ", column " + column + ": Could not match a valid syntax element");
			}
		}

		stream.append(new Token("", Token.Type.EOF));
		return stream;
	}
	
	public TokenStream getLastTokens(){
		return stream;
	}
	
}
