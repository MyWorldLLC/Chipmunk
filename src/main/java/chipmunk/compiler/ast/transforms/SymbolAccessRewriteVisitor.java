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

import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.Symbol;
import chipmunk.compiler.SymbolTable;
import chipmunk.compiler.UnresolvedSymbolException;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.*;

/**
 * Variables can be in module, class, or local scope. The code generator
 * can determine this when emitting read/write ops for the variables, but
 * the logic for that gets messy. Doing this as an AST rewrite simplifies
 * the code generator because all accesses flow through the self reference.
 *
 * Example:
 * Module: f() -> self.getModule().f()
 * Class (shared): f() -> self.getClass().f()
 * Class (instance): f() -> self.f()
 * Local: f() -> f() (unmodified)
 */
public class SymbolAccessRewriteVisitor implements AstVisitor {

    protected SymbolTable scope;

    @Override
    public void visit(AstNode node) {

        if(node instanceof BlockNode){
            scope = ((BlockNode) node).getSymbolTable();
        }else if(node instanceof OperatorNode){
            OperatorNode opNode = (OperatorNode) node;
            // Recurse to find all "terminal left" symbols & rewrite all symbol accesses
            // that are non-local
            if(opNode.getLeft() instanceof IdNode){
                // Terminal left id node - check & rewrite access if needed
                IdNode varId = (IdNode) opNode.getLeft();
                String symbolName = varId.getName();
                Symbol symbol = scope.getSymbol(symbolName);

                final int index = varId.getID().getIndex();
                final int line = varId.getID().getLine();
                final int column = varId.getID().getColumn();

                if(symbol == null){
                    throw new UnresolvedSymbolException(scope.getModuleScope().getDebugSymbol(), symbolName);
                }

                if(symbol.getTable().isMethodScope()){
                    // No rewrite needed because this is a local variable
                    return;
                }

                // If the symbol is found in the module scope AND we are in a class-scope method,
                // call getModule() & emit access at module level
                if(symbol.getDeclaringScope() == SymbolTable.Scope.MODULE){

                    // Method reference to a module-level symbol
                    // Rewrite to self.getModule().symbol
                    OperatorNode getModuleCallNode = new OperatorNode(new Token("(", Token.Type.LPAREN, index, line, column));
                    OperatorNode selfDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));
                    OperatorNode varDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));

                    IdNode self = new IdNode(new Token("self", Token.Type.IDENTIFIER, index, line, column));

                    IdNode getModule = new IdNode(new Token("getModule", Token.Type.IDENTIFIER, index, line, column));

                    selfDotNode.getChildren().add(self);
                    selfDotNode.getChildren().add(getModule);

                    getModuleCallNode.getChildren().add(selfDotNode);

                    varDotNode.getChildren().add(getModuleCallNode);
                    if(symbol.isImported()){
                        // If this symbol is imported, we have to rewrite access to
                        // self.getModule().$module_field_name.symbol
                        OperatorNode importDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));

                        final String moduleFieldName = ChipmunkCompiler.importedModuleName(symbol.getImport().getModule());
                        IdNode importedModuleName = new IdNode(new Token(moduleFieldName, Token.Type.IDENTIFIER, index, line, column));

                        if(symbol.getImport().isAliased()){
                            varId = new IdNode(new Token(symbol.getImport().getAlias(), Token.Type.IDENTIFIER, index, line, column));
                        }

                        varDotNode.getChildren().add(importedModuleName);
                        importDotNode.getChildren().add(varDotNode);
                        importDotNode.getChildren().add(varId);

                        varDotNode = importDotNode;
                    }else{
                        varDotNode.getChildren().add(varId);
                    }

                    opNode.getChildren().set(0, varDotNode);

                    node.visitChildren(this, 1);
                    return;
                }else if(symbol.getTable().getScope() == SymbolTable.Scope.CLASS){

                    // Symbol is defined in the class - emit a shared or instance fetch

                    if(symbol.isShared()){
                        // Symbol is a shared field
                        // Rewrite to self.getClass().symbol

                        OperatorNode getClassCallNode = new OperatorNode(new Token("(", Token.Type.LPAREN, index, line, column));
                        OperatorNode selfDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));
                        OperatorNode varDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));

                        IdNode self = new IdNode(new Token("self", Token.Type.IDENTIFIER, index, line, column));

                        IdNode getClass = new IdNode(new Token("getClass", Token.Type.IDENTIFIER, index, line, column));

                        selfDotNode.getChildren().add(self);
                        selfDotNode.getChildren().add(getClass);

                        getClassCallNode.getChildren().add(selfDotNode);

                        varDotNode.getChildren().add(getClassCallNode);
                        varDotNode.getChildren().add(varId);

                        opNode.getChildren().set(0, varDotNode);

                        node.visitChildren(this, 1);
                        return;
                    }else{
                        // Symbol is an instance field
                        // Rewrite to self.symbol

                        OperatorNode selfDotNode = new OperatorNode(new Token(".", Token.Type.DOT, index, line, column));

                        IdNode self = new IdNode(new Token("self", Token.Type.IDENTIFIER, index, line, column));
                        selfDotNode.getChildren().add(self);
                        selfDotNode.getChildren().add(varId);

                        opNode.getChildren().set(0, selfDotNode);

                        node.visitChildren(this, 1);
                        return;
                    }

                }
            }
        }

        node.visitChildren(this);

    }
}
