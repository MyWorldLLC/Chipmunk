package chipmunk.compiler;

import java.util.regex.Pattern;

public class Token {
	
	protected String text;
	protected Type type;

	protected final int index;
	protected final int line;
	protected final int column;
	
	/**
	 * Defines the token types that the lexer will produce. NOTE: certain tokens, such as the floating point literal 1.23,
	 * are ambiguous as they may be matched multiple ways. For example, 1.23 could be matched as the tokens INT DOT INT.
	 * Logical AND (&&) could be matched as AMP AMP. To avoid this, ALWAYS list the single ambiguous token before the token
	 * that could start a valid sequence of alternative tokens so that the lexer will attempt to match them first. For example,
	 * FLOATLITERAL is listed before INTLITERAL and all keywords are listed before IDENTIFIER for this reason.
	 * @author Daniel Perano
	 */
	public enum Type {
		// comments and newlines
		COMMENT("#.*"), NEWLINE("\n|\r|\r\n"),
		
		// literals
		// Binary, octal, and hex literals must go before the int and float literals
		// or the leading 0s get matched as ints
		BINARYLITERAL("(0b|0B)[01_]+", false, true),
		OCTLITERAL("(0o|0O)[0-7_]+", false, true),
		HEXLITERAL("(0x|0X)[a-fA-F0-9_]+", false, true),
		FLOATLITERAL("-?[0-9]*\\.[0-9]+((e|E)-?[0-9]+)?", false, true),
		INTLITERAL("-?[0-9_]+", false, true),
		BOOLLITERAL("true|false", true, true),
		STRINGLITERAL("\"(\\\\\"|[^\"])*\"|'(\\\\\'|[^\'])*'", false, true),
		
		// blocks, indexing, grouping, and calling
		LBRACE("\\{"), RBRACE("\\}"), LBRACKET("\\["), RBRACKET("\\]"), LPAREN("\\("), RPAREN("\\)"), COMMA(","),
		
		// symbols - multiple and single forms
		DOUBLEPLUSEQUALS("\\+\\+\\="), PLUSEQUALS("\\+\\="), DOUBLEMINUSEQUALS("\\-\\-\\="), MINUSEQUALS("\\-\\="), DOUBLESTAREQUALS("\\*\\*\\="), STAREQUALS("\\*="),
		DOUBLEFSLASHEQUALS("//\\="), FSLASHEQUALS("/\\="), PERCENTEQUALS("%\\="), DOUBLEAMPERSANDEQUALS("&&\\="), AMPERSANDEQUALS("&\\="),
		CARETEQUALS("\\^\\="), DOUBLEBAREQUALS("\\|\\|\\="), BAREQUALS("\\|\\="), DOUBLELESSEQUALS("<<\\="), LESSEQUALS("<\\="), TRIPLEMOREQUALS(">>>\\="),
		DOUBLEMOREEQUALS(">>\\="), MOREEQUALS(">\\="), EXCLAMATIONEQUALS("\\!\\="), TILDEEQUALS("~\\="), COLON(":"),
		DOUBLEEQUAlS("\\=\\="), EQUALS("\\="), DOUBLEDOTLESS("\\.\\.<"), DOUBLEDOT("\\.\\."), DOT("\\."), DOUBLESTAR("\\*\\*"),
		STAR("\\*"), DOUBLEPLUS("\\+\\+"),PLUS("\\+"), DOUBLEMINUS("\\-\\-"), MINUS("\\-"), DOUBLEFSLASH("//"), FSLASH("/"),
		DOUBLEBAR("\\|\\|"), BAR("\\|"), EXCLAMATION("\\!"), TILDE("~"), CARET("\\^"),
		DOUBLELESSTHAN("<<"), LESSTHAN("<"), TRIPLEMORETHAN(">>>"), DOUBLEMORETHAN(">>"), MORETHAN(">"),
		PERCENT("%"), DOUBLEAMPERSAND("&&"), AMPERSAND("&"),
		
		
		// keywords - (?![a-zA-Z0-9_]) checks that the following character is not alphanumeric or an underscore, the
		// presence of which would make the token an identifier instead of a keyword
		MODULE("module(?![a-zA-Z0-9_])", true, false),
		FROM("from(?![a-zA-Z0-9_])", true, false), IMPORT("import(?![a-zA-Z0-9_])", true, false), AS("as(?![a-zA-Z0-9_])", true, false),
		IN("in(?![a-zA-Z0-9_])", true, false), CLASS("class(?![a-zA-Z0-9_])", true, false), SHARED("shared(?![a-zA-Z0-9_])", true, false),
		NULL("null(?![a-zA-Z0-9_])", true, false), IF("if(?![a-zA-Z0-9_])", true, false), ELSE("else(?![a-zA-Z0-9_])", true, false),
		FOR("for(?![a-zA-Z0-9_])", true, false), WHILE("while(?![a-zA-Z0-9_])", true, false), BREAK("break(?![a-zA-Z0-9_])", true, false),
		CONTINUE("continue(?![a-zA-Z0-9_])", true, false), RETURN("return(?![a-zA-Z0-9_])", true, false), TRY("try(?![a-zA-Z0-9_])", true, false),
		CATCH("catch(?![a-zA-Z0-9_])", true, false), FINALLY("finally(?![a-zA-Z0-9_])"), THROW("throw(?![a-zA-Z0-9_])", true, false), DEF("def(?![a-zA-Z0-9_])", true, false),
		VAR("var(?![a-zA-Z0-9_])", true, false), TRAIT("trait(?![a-zA-Z0-9_])", true, false), FINAL("final(?![a-zA-Z0-9_])", true, false),
		
		// identifiers go second to last so that they don't interfere with matching keywords
		IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),
		// EOF goes last
		EOF("");
		
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
		this(token, tokenType, -1, -1, -1);
	}
	
	public Token(String token, Type tokenType, int index, int line, int column){
		text = token;
		type = tokenType;
		this.index = index;
		this.line = line;
		this.column = column;
	}
	
	public String getText(){
		return text;
	}
	
	public Type getType(){
		return type;
	}

	public int getIndex() {
		return index;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
	
	@Override
	public String toString(){
		return text.trim() + "(" + type.name().toLowerCase() + ")";
	}

}
