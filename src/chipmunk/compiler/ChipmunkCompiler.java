package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChipmunkCompiler {
	
	protected TokenStream stream;
	
	public ChipmunkCompiler(){
		stream = new TokenStream();
	}
	
	public void lex(InputStream srcStream){
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(srcStream, Charset.forName("UTF-8")));
		StringBuilder builder = new StringBuilder();
		
		// copy chars from input stream to StringBuilder
		try{
			
			char[] copyChars = new char[100];
			
			int read = reader.read(copyChars, 0, copyChars.length);
			
			while(read != -1){
				
				builder.append(copyChars, 0, read);
				
				read = reader.read(copyChars, 0, copyChars.length);
				
			}
			
			
		}catch(IOException ex){
			throw new CompileChipmunk("I/O error while reading source: ", ex);
		}
		
		lex(builder);
	}
	
	public void lex(CharSequence src){
		
		// line/column identification of tokens
		int line = 1;
		int column = 1;
		
		// create a matcher cache so we don't have to keep creating and discarding them
		Map<Token.Type, Matcher> matchers = new HashMap<Token.Type, Matcher>();
		
		Token.Type[] tokenTypes = Token.Type.values();
		for(int i = 0; i < tokenTypes.length; i++){
			Token.Type type = tokenTypes[i];
			matchers.put(type, type.getPattern().matcher(src));
		}
		
		// spaces
		Pattern spacePattern = Pattern.compile(" ");
		Matcher space = spacePattern.matcher(src);
		
		Pattern tabPattern = Pattern.compile("\\t");
		Matcher tab = tabPattern.matcher(src);
		
		int cursor = 0;
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
			
			boolean foundOne = false;
			// try to match one of our tokens
			for(int i = 0; i < tokenTypes.length; i++){
				
				Token.Type type = tokenTypes[i];
				Matcher matcher = matchers.get(type);
				matcher.region(cursor, src.length() - 1);
				
				if(matcher.lookingAt()){
					foundOne = true;
					
					String subStr = src.subSequence(cursor, matcher.end()).toString();
					stream.append(new Token(subStr, type, line, column));
					
					cursor += matcher.end();
					if(type == Token.Type.NEWLINE){
						column = 1;
						line += 1;
					}else{
						column += matcher.end();
					}
					break;
				}
			}
			if(!foundOne){
				// error - we couldn't match a valid token
				throw new SyntaxErrorChipmunk("Syntax error at line " + line + ", column " + column + ": Could not match a valid syntax element");
			}
		}

		stream.append(new Token("", Token.Type.EOF));
	}

}
