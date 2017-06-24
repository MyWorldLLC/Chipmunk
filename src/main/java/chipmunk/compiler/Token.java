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
		LESSTHAN("<"), MORETHAN(">"), PERCENT("%"), AMPERSAND("&"), INTLITERAL("-?[0-9_]+", false, true),
		BINARYLITERAL("0b|0B[01_]+", false, true), OCTLITERAL("0o|0O[0-7_]+", false, true), HEXLITERAL("0x|0X[a-fA-F0-9_]+", false, true),
		FLOATLITERAL("-?[0-9]*\\.?[0-9]*((e|E)-?[0-9]+)?", false, true), BOOLLITERAL("true|false", false, true),
		STRINGLITERAL("\"([^\"]|\\\")*\"|'([^']|\\')*'", false, true), IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"), MODULE("module", true, false),
		FROM("from", true, false), IMPORT("import", true, false), AS("as", true, false), IN("in", true, false),
		CLASS("class", true, false), SHARED("shared", true, false), NEW("new", true, false), NULL("null", true, false),
		IF("if", true, false), ELSE("else", true, false), FOR("for", true, false), WHILE("while", true, false),
		BREAK("break", true, false), CONTINUE("continue", true, false), RETURN("return", true, false),
		TRY("try", true, false), CATCH("catch", true, false), THROW("throw", true, false), DEF("def", true, false),
		VAR("var", true, false), EOF("");
		
		protected Pattern pattern;
		protected boolean keyword;
		protected boolean literal;
		
		Type(String regex){
			this(regex, false, false);
		}
		
		Type(String regex, boolean keyword, boolean literal){
			pattern = Pattern.compile(regex);
			this.keyword = keyword;
			this.literal = literal;
		}
		
		public Pattern getPattern(){
			return pattern;
		}
		
		public boolean isKeyword(){
			return keyword;
		}
		
		public boolean isLiteral(){
			return literal;
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
