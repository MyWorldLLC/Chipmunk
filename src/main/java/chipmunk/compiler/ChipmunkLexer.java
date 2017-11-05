package chipmunk.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChipmunkLexer {
	
	protected TokenStream stream;
	protected CharSequence src;
	
	private int line;
	private int lineBegin;
	private int column;
	private int cursor;

	public TokenStream lex(CharSequence source){
		
		stream = new TokenStream();
		
		src = source;
		
		lineBegin = 0;
		line = 1;
		column = 1;
		cursor = 0;
		
		// fill matcher cache
		Map<Token.Type, Matcher> matchers = new HashMap<Token.Type, Matcher>();
		
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
		
		while(cursor < src.length()){
			
			// check for spaces
			space.region(cursor, src.length());
			if(space.lookingAt()){
				cursor += 1;
				column += 1;
				continue;
			}
			
			// check for tabs
			tab.region(cursor, src.length());
			if(tab.lookingAt()){
				cursor += 1;
				column += 4;
				continue;
			}
			
			// if skipping the whitespace brings us to the end of the source, break
			if(cursor == src.length()){
				break;
			}
			
			boolean foundToken = false;
			
			// try to match correct token
			for(int i = 0; i < tokenTypes.length - 1; i++){
				
				Token.Type type = tokenTypes[i];

				Matcher matcher = matchers.get(type);
				matcher.region(cursor, src.length());
				
				if (matcher.lookingAt()){

					String subStr = src.subSequence(cursor, matcher.end()).toString();
					stream.append(new Token(subStr, type, line, column));

					cursor = matcher.end();

					if (type == Token.Type.NEWLINE) {
						column = 1;
						line += 1;
						lineBegin = cursor;
					} else {
						column += matcher.end() - lineBegin;
					}

					foundToken = true;
					break;

				}
			}
			
			// error - we couldn't match a valid token
			if(!foundToken && cursor != source.length() - 1){
				throw new SyntaxErrorChipmunk("Syntax error at line " + line + ", column " + column + ": Could not match a valid syntax element");
			}
		}

		stream.append(new Token("", Token.Type.EOF, line, column));
		return stream;
	}
	
	public TokenStream getLastTokens(){
		return stream;
	}
	
}
