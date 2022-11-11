/*
 * Copyright (C) 2022 MyWorld, LLC
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

import java.util.regex.Pattern;

/**
 * Defines the token types that the lexer will produce. NOTE: certain tokens, such as the floating point literal 1.23,
 * are ambiguous as they may be matched multiple ways. For example, 1.23 could be matched as the tokens INT DOT INT.
 * Logical AND (&&) could be matched as AMP AMP. To avoid this, ALWAYS list the single ambiguous token before the token
 * that could start a valid sequence of alternative tokens so that the lexer will attempt to match them first. For example,
 * FLOATLITERAL is listed before INTLITERAL and all keywords are listed before IDENTIFIER for this reason.
 *
 * @author Daniel Perano
 */
public enum TokenType {
    // comments and newlines
    COMMENT("#.*"), NEWLINE("\n|\r|\r\n"),

    // literals
    // Binary, octal, and hex literals must go before the int and float literals
    // or the leading 0s get matched as ints
    BINARYLITERAL("(0b|0B)[01_]+", false, true),
    OCTLITERAL("(0o|0O)[0-7_]+", false, true),
    HEXLITERAL("(0x|0X)[a-fA-F0-9_]+", false, true),
    FLOATLITERAL("[0-9]*\\.[0-9]+((e|E)-?[0-9]+)?", false, true),
    INTLITERAL("[0-9][0-9_]*", false, true),
    BOOLLITERAL("true|false", true, true),
    STRINGLITERAL("\"(\\\\\"|[^\"])*\"|'(\\\\\'|[^\'])*'", false, true),

    // blocks, indexing, grouping, and calling
    LBRACE("\\{"), RBRACE("\\}"),
    LBRACKET("\\["), RBRACKET("\\]"),
    LPAREN("\\("), RPAREN("\\)"),
    COMMA(","),

    // symbols - multiple and single forms
    DOUBLEPLUSEQUALS("\\+\\+\\="), PLUSEQUALS("\\+\\="),
    DOUBLEMINUSEQUALS("\\-\\-\\="), MINUSEQUALS("\\-\\="),
    DOUBLESTAREQUALS("\\*\\*\\="), STAREQUALS("\\*="),
    DOUBLEFSLASHEQUALS("//\\="), FSLASHEQUALS("/\\="),
    PERCENTEQUALS("%\\="),
    DOUBLEAMPERSANDEQUALS("&&\\="), AMPERSANDEQUALS("&\\="),
    CARETEQUALS("\\^\\="),
    DOUBLEBAREQUALS("\\|\\|\\="), BAREQUALS("\\|\\="),
    DOUBLELESSEQUALS("<<\\="), LESSEQUALS("<\\="),
    TRIPLEMOREQUALS(">>>\\="), DOUBLEMOREEQUALS(">>\\="), MOREEQUALS(">\\="),
    EXCLAMATIONEQUALS("\\!\\="),
    TILDEEQUALS("~\\="),
    DOUBLECOLON("::"),
    COLON(":"),
    DOUBLEEQUAlS("\\=\\="),
    EQUALS("\\="),
    DOUBLEDOTLESS("\\.\\.<"),
    DOUBLEDOT("\\.\\."), DOT("\\."),
    DOUBLESTAR("\\*\\*"), STAR("\\*"),
    DOUBLEPLUS("\\+\\+"), PLUS("\\+"),
    DOUBLEMINUS("\\-\\-"), MINUS("\\-"),
    DOUBLEFSLASH("//"), FSLASH("/"),
    DOUBLEBAR("\\|\\|"), BAR("\\|"),
    EXCLAMATION("\\!"),
    TILDE("~"),
    CARET("\\^"),
    DOUBLELESSTHAN("<<"), LESSTHAN("<"),
    TRIPLEMORETHAN(">>>"), DOUBLEMORETHAN(">>"), MORETHAN(">"),
    PERCENT("%"),
    DOUBLEAMPERSAND("&&"), AMPERSAND("&"),


    // keywords - (?![a-zA-Z0-9_]) checks that the following character is not alphanumeric or an underscore, the
    // presence of which would make the token an identifier instead of a keyword
    MODULE("module", true),
    FROM("from", true),
    IMPORT("import", true),
    AS("as", true),
    IN("in", true),
    CLASS("class", true),
    SHARED("shared", true),
    NULL("null", true),
    IF("if", true),
    ELSE("else", true),
    FOR("for", true),
    WHILE("while", true),
    BREAK("break", true),
    CONTINUE("continue", true),
    RETURN("return", true),
    TRY("try", true),
    CATCH("catch", true),
    FINALLY("finally", true),
    THROW("throw", true),
    DEF("def", true),
    VAR("var", true),
    TRAIT("trait", true),
    FINAL("final", true),
    INSTANCEOF("instanceof", true),
    IS("is", true),
    MATCH("match", true),
    CASE("case", true),
    WHEN("when", true),

    // identifiers go second to last so that they don't interfere with matching keywords
    IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),
    // EOF goes last
    EOF("");

    protected final Pattern pattern;
    protected final boolean keyword;
    protected final boolean literal;

    TokenType(String regex) {
        this(regex, false, false);
    }

    TokenType(String regex, boolean keyword) {
        this(regex, keyword, false);
    }

    TokenType(String regex, boolean keyword, boolean literal) {
        pattern = Pattern.compile(keyword ? regex + "(?![a-zA-Z0-9_])" : regex);
        this.keyword = keyword;
        this.literal = literal;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public boolean isLiteral() {
        return literal;
    }

}
