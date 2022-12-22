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

package chipmunk.compiler.ast;

import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;

public class Modules {

    public static AstNode make(String name){
        return make(new Token("module", TokenType.MODULE), name);
    }

    public static AstNode make(Token token, String name){
        return make(token, new Token(name, TokenType.IDENTIFIER));
    }

    public static AstNode make(Token token, Token name){
        AstNode node = new AstNode(NodeType.MODULE, token);
        node.setSymbol(new Symbol(name.text()));
        return node;
    }

}
