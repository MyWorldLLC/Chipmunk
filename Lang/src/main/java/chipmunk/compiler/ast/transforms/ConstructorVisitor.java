/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;

public class ConstructorVisitor implements AstVisitor {
    @Override
    public void visit(AstNode node) {

        if(node.is(NodeType.CLASS)){

            Symbol constructorSymbol = node.getSymbolTable().getSymbolLocal(node.getSymbol().getName());
            if(constructorSymbol == null){
                var constructor = Methods.make("$" + node.getSymbol().getName());
                Methods.visitParams(constructor, p -> constructor.getSymbolTable().setSymbol(new Symbol(VarDec.getVarName(p))));

                node.addChild(constructor);
                node.getSymbolTable().setSymbol(constructor.getSymbol());
                constructor.getSymbolTable().setScope(SymbolTable.Scope.METHOD);
                constructor.getSymbolTable().setParent(node.getSymbolTable());

                constructorSymbol = constructor.getSymbol();

            }else{
                constructorSymbol.setName("$" + constructorSymbol.getName());
            }

            Methods.addToBody(constructorSymbol.getReferent(), 0,
                    Methods.makeInvocation(Identifier.make("self"), "$instance_init$"));
        }

        node.visitChildren(this);

        // Note: We have to add a self-return here or for trivial constructors the lambda rewrite will add a default (non-self) return, which we don't want
        if(node.is(NodeType.CLASS)){
            var constructor = node.getSymbolTable().getSymbolLocal("$" + node.getSymbol().getName()).getReferent();
            Methods.addToBody(constructor, new AstNode(NodeType.FLOW_CONTROL, new Token("return", TokenType.RETURN), Identifier.make("self")));
        }
    }
}
