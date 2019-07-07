package chipmunk.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChipmunkLexer {
	
	protected TokenStream stream;
	protected CharSequence src;

	public TokenStream lex(CharSequence source){
		
		if(source == null) {
			throw new NullPointerException("Source cannot be null");
		}
		
		stream = new TokenStream();
		
		src = source;
		
		int line = 1;
		int column = 1;
		int cursor = 0;
		
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

					String subStr = src.subSequence(matcher.start(), matcher.end()).toString();
					stream.append(new Token(subStr, type, line, column));
					cursor = matcher.end();

					if (type == Token.Type.NEWLINE) {
						column = 1;
						line += 1;
					} else {
						column += matcher.end() - matcher.start();
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
	
    /**
     * Thanks to Udo Klimaschewski: https://gist.github.com/uklimaschewski/6741769
     * Unescapes a string that contains standard Java escape sequences.
     * <ul>
     * <li><strong>&#92;b &#92;f &#92;n &#92;r &#92;t &#92;" &#92;'</strong> :
     * BS, FF, NL, CR, TAB, double and single quote.</li>
     * <li><strong>&#92;X &#92;XX &#92;XXX</strong> : Octal character
     * specification (0 - 377, 0x00 - 0xFF).</li>
     * <li><strong>&#92;uXXXX</strong> : Hexadecimal based Unicode character.</li>
     * </ul>
     * 
     * @param st A string optionally containing standard java escape sequences.
     * @return The translated string.
     */
    public static String unescapeString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                case '\\':
                    ch = '\\';
                    break;
                case 'b':
                    ch = '\b';
                    break;
                case 'f':
                    ch = '\f';
                    break;
                case 'n':
                    ch = '\n';
                    break;
                case 'r':
                    ch = '\r';
                    break;
                case 't':
                    ch = '\t';
                    break;
                case '\"':
                    ch = '\"';
                    break;
                case '\'':
                    ch = '\'';
                    break;
                // Hex Unicode: u????
                case 'u':
                    if (i >= st.length() - 5) {
                        ch = 'u';
                        break;
                    }
                    int code = Integer.parseInt(
                            "" + st.charAt(i + 2) + st.charAt(i + 3)
                                    + st.charAt(i + 4) + st.charAt(i + 5), 16);
                    sb.append(Character.toChars(code));
                    i += 5;
                    continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
}
	
}
