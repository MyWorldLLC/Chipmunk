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

package chipmunk.compiler.parser;

import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.parser.parselets.*;

import java.util.HashMap;
import java.util.Map;

public class ExpressionParser {

    private final Map<TokenType, InfixParselet> infix = new HashMap<>();
    private final Map<TokenType, PrefixParselet> prefix = new HashMap<>();
    protected final TokenStream tokens;

    public ExpressionParser(TokenStream tokens){
        this.tokens = tokens;
        // register parselets

        // identifiers and literals
        register(TokenType.IDENTIFIER, new NameParselet());
        register(TokenType.BOOLLITERAL, new LiteralParselet());
        register(TokenType.BINARYLITERAL, new LiteralParselet());
        register(TokenType.HEXLITERAL, new LiteralParselet());
        register(TokenType.OCTLITERAL, new LiteralParselet());
        register(TokenType.INTLITERAL, new LiteralParselet());
        register(TokenType.FLOATLITERAL, new LiteralParselet());
        register(TokenType.STRINGLITERAL, new LiteralParselet());
        register(TokenType.NULL, new LiteralParselet());
        register(TokenType.LBRACKET, new ListParselet());
        register(TokenType.LBRACE, new MapParselet());

        // prefix operators
        prefixOp(TokenType.PLUS);
        prefixOp(TokenType.MINUS);
        prefixOp(TokenType.DOUBLEPLUS);
        prefixOp(TokenType.DOUBLEMINUS);
        prefixOp(TokenType.EXCLAMATION);
        prefixOp(TokenType.TILDE);

        // parentheses for grouping in expressions
        register(TokenType.LPAREN, new GroupingParselet());

        // binary infix operators
        register(TokenType.PLUS, new AddSubOperatorParselet());
        register(TokenType.MINUS, new AddSubOperatorParselet());
        register(TokenType.STAR, new MulDivOperatorParselet());
        register(TokenType.FSLASH, new MulDivOperatorParselet());
        register(TokenType.DOUBLEFSLASH, new MulDivOperatorParselet());
        register(TokenType.PERCENT, new MulDivOperatorParselet());

        register(TokenType.DOUBLESTAR, new PowerOperatorParselet());

        register(TokenType.DOT, new DotOperatorParselet());
        register(TokenType.AS, new CastOperatorParselet());

        register(TokenType.DOUBLELESSTHAN, new ShiftRangeOperatorParselet());
        register(TokenType.DOUBLEMORETHAN, new ShiftRangeOperatorParselet());
        register(TokenType.DOUBLEDOTLESS, new ShiftRangeOperatorParselet());
        register(TokenType.DOUBLEDOT, new ShiftRangeOperatorParselet());

        register(TokenType.LESSTHAN, new LesserGreaterInstanceOfOperatorParselet());
        register(TokenType.LESSEQUALS, new LesserGreaterInstanceOfOperatorParselet());
        register(TokenType.MORETHAN, new LesserGreaterInstanceOfOperatorParselet());
        register(TokenType.MOREEQUALS, new LesserGreaterInstanceOfOperatorParselet());
        register(TokenType.INSTANCEOF, new LesserGreaterInstanceOfOperatorParselet());

        register(TokenType.IS, new EqualityOperatorParselet());
        register(TokenType.DOUBLEEQUAlS, new EqualityOperatorParselet());
        register(TokenType.EXCLAMATIONEQUALS, new EqualityOperatorParselet());

        register(TokenType.AMPERSAND, new BitAndOperatorParselet());
        register(TokenType.BAR, new BitOrOperatorParselet());
        register(TokenType.CARET, new BitXOrOperatorParselet());

        register(TokenType.DOUBLEAMPERSAND, new AndOperatorParselet());
        register(TokenType.DOUBLEBAR, new OrOperatorParselet());
        register(TokenType.DOUBLECOLON, new BindingOperatorParselet());

        register(TokenType.EQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEPLUSEQUALS, new AssignOperatorParselet());
        register(TokenType.PLUSEQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEMINUSEQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLESTAREQUALS, new AssignOperatorParselet());
        register(TokenType.STAREQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEFSLASHEQUALS, new AssignOperatorParselet());
        register(TokenType.FSLASHEQUALS, new AssignOperatorParselet());
        register(TokenType.PERCENTEQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEAMPERSANDEQUALS, new AssignOperatorParselet());
        register(TokenType.AMPERSANDEQUALS, new AssignOperatorParselet());
        register(TokenType.CARETEQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEBAREQUALS, new AssignOperatorParselet());
        register(TokenType.BAREQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLELESSEQUALS, new AssignOperatorParselet());
        register(TokenType.TRIPLEMOREQUALS, new AssignOperatorParselet());
        register(TokenType.DOUBLEMOREEQUALS, new AssignOperatorParselet());
        register(TokenType.TILDEEQUALS, new AssignOperatorParselet());

        // postfix operators
        register(TokenType.DOUBLEPLUS, new PostIncDecParselet());
        register(TokenType.DOUBLEMINUS, new PostIncDecParselet());
        register(TokenType.LPAREN, new CallOperatorParselet());
        register(TokenType.LBRACKET, new IndexOperatorParselet());

        // method def operator (allow method definitions in expressions)
        register(TokenType.DEF, new MethodDefParselet(tokens));
        // class definition operator (allows creating anonymous classes in expressions)
        //register(Token.Type.CLASS, new ClassDefParselet());
    }

    public TokenStream getTokens(){
        return tokens;
    }

    protected void register(TokenType type, InfixParselet parselet){
        infix.put(type, parselet);
    }

    protected void register(TokenType type, PrefixParselet parselet){
        prefix.put(type, parselet);
    }

    protected void prefixOp(TokenType op){
        prefix.put(op, new PrefixOperatorParselet());
    }

    /**
     * Parse expressions with precedence climbing algorithm
     * @return AST of the expression
     */
    public AstNode parseExpression(){
        return parseExpression(0);
    }

    public AstNode parseExpression(int minPrecedence){

        tokens.skipNewlinesAndComments();

        Token token = tokens.get();

        PrefixParselet prefixParser = prefix.get(token.type());

        if(prefixParser == null){
            ChipmunkParser.syntaxError("expression", tokens.getFileName(), "literal, id, or unary operator", token);
        }

        AstNode left = prefixParser.parse(this, token);

        tokens.skipNewlinesAndComments();

        token = tokens.peek();
        while(minPrecedence < getPrecedence(token)){
            token = tokens.get();

            InfixParselet infixParser = infix.get(token.type());

            if(infixParser == null){
                ChipmunkParser.syntaxError("expression", tokens.getFileName(), "literal, id, or binary operator", token);
            }

            left = infixParser.parse(this, left, token);
            tokens.skipNewlinesAndComments();
            token = tokens.peek();
        }

        tokens.skipNewlinesAndComments();

        return left;
    }

    private int getPrecedence(Token token){
        InfixParselet parselet = infix.get(token.type());
        if(parselet != null){
            return parselet.getPrecedence();
        }else{
            return 0;
        }
    }

}
