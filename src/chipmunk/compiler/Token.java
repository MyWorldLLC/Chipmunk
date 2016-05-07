package chipmunk.compiler;

import java.util.regex.Pattern;

public class Token {
	
	protected String text;
	protected Type type;
	
	protected String file;
	protected int line;
	protected int column;
	
	public enum Type {
		LBRACE("{"), RBRACE("}"), LBRACKET("["), RBRACKET("]"), LPAREN("("), RPAREN(")"),
		COMMA(","), SEMICOLON(";"), NEWLINE("\n|\r\n|\r"), EQUALS("="), DOT("."), STAR("*"), PLUS("+"),
		MINUS("-"), FSLASH("/"), BSLASH("\\"), BAR("|"), EXCLAMATION("!"), POUND("#"), TILDE("~"), CARET("^"),
		LESSTHAN("<"), MORETHAN(">"), PERCENT("%"), AMPERSAND("&"), INTLITERAL("[+?-?][0-9]+"),
		BINARYLITERAL("0b|0B[01]+"), HEXLITERAL("0x|0X[a-fA-F0-9]+"), FLOATLITERAL("[-+]?[0-9]+\\.[0-9]+([eE][-+]?[0-9]+)?"),
		BOOLLITERAL("true|false"), STRINGLITERAL("\"([^\"]|\\\")*\"|'([^']|\\')*'"),
		IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"), FROM("from"), IMPORT("import"), AS("as"), IN("in"), CLASS("class"),
		SHARED("shared"), NEW("new"), EOF("");
		
		protected Pattern pattern;
		
		Type(String regex){
			pattern = Pattern.compile(regex);
		}
		
		public Pattern getPattern(){
			return pattern;
		}
		
	}
	
	public Token(String token, Type tokenType){
		this(token, tokenType, -1, -1);
	}
	
	public Token(String token, Type tokenType, int line, int column){
		text = token;
		type = tokenType;
		this.line = line;
		this.column = column;
	}
	
	public String getText(){
		return text;
	}
	
	public Type getType(){
		return type;
	}
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

}
