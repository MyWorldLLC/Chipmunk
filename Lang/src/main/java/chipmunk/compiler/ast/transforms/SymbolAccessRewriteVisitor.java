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
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
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

        if(node.is(NodeType.IMPORT)){
            return;
        }

        if(node.getType().isBlock()){
            scope = node.getSymbolTable();
        }

        // Recurse to find all non-qualified terminal symbols & rewrite all symbol accesses
        // that are non-local

        // If visiting a variable declaration, don't rewrite the variable name being declared!
        int startIndex = node.is(NodeType.VAR_DEC) ? 1 : 0;
        for(int i = startIndex; i < node.childCount(); i++) {
            AstNode child = node.getChild(i);

            if (child.is(NodeType.ID) && !isQualified(node, child)) {

                if (!isMethodBindTarget(node, i)) {

                    child = rewriteQualified(child);
                    node.replaceChild(i, child);
                    System.out.println(node);
                }
            } else {
                child.visit(this);
            }
        }

        if(node.getType().isBlock()){
            scope = scope.getParent();
        }

    }

    protected boolean isQualified(AstNode parent, AstNode child){
        if(parent.is(NodeType.OPERATOR)){
            return parent.getToken().text().equals(".") && parent.getRight() == child;
        }
        return false;
    }

    protected AstNode rewriteQualified(AstNode child) {

        // Terminal id node - check & rewrite access if needed
        String symbolName = child.getToken().text();
        Symbol symbol = scope.getSymbol(symbolName);

        final int index = child.getToken().index();
        final int line = child.getToken().line();
        final int column = child.getToken().column();

        if (symbol == null) {
            throw new UnresolvedSymbolException(scope.getModuleScope().getDebugSymbol(), symbolName);
        }

        if (symbol.getTable().isMethodScope()) {
            // Mark local variables that are in an outer method scope as closures
            if(scope.isClosured(symbol)){
                symbol.markAsClosure();
            }
            // No rewrite needed because this is a local variable
            return child;
        }

        // If the symbol is found in the module scope call getModule() & emit access at module level
        if (symbol.getDeclaringScope() == SymbolTable.Scope.MODULE) {

            // Method reference to a module-level symbol
            // Rewrite to self.getModule().symbol
            AstNode getModuleCallNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, index, line, column));
            AstNode selfDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));
            AstNode varDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));

            AstNode self = new AstNode(NodeType.ID, new Token("self", TokenType.IDENTIFIER, index, line, column));

            AstNode getModule = new AstNode(NodeType.ID, new Token("getModule", TokenType.IDENTIFIER, index, line, column));

            selfDotNode.addChild(self);
            selfDotNode.addChild(getModule);

            getModuleCallNode.addChild(selfDotNode);

            varDotNode.addChild(getModuleCallNode);
            if (symbol.isImported()) {
                // If this symbol is imported, we have to rewrite access to
                // self.getModule().$module_field_name.symbol
                AstNode importDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));

                final String moduleFieldName = ChipmunkCompiler.importedModuleName(symbol.getImport().getModule());
                AstNode importedModuleName = new AstNode(NodeType.ID, new Token(moduleFieldName, TokenType.IDENTIFIER, index, line, column));

                if (symbol.getImport().isAliased()) {
                    child = new AstNode(NodeType.ID, new Token(symbol.getImport().getAliasedSymbol(), TokenType.IDENTIFIER, index, line, column));
                }

                varDotNode.addChild(importedModuleName);
                importDotNode.addChild(varDotNode);
                importDotNode.addChild(child);

                varDotNode = importDotNode;
            } else {
                varDotNode.addChild(child);
            }

            return varDotNode;
        } else if (symbol.getDeclaringScope() == SymbolTable.Scope.CLASS) {

            // Symbol is defined in the class - emit a shared or instance fetch

            if (symbol.isShared() && !scope.isSharedMethodScope()) {
                // Symbol is a shared field AND we are not accessing it from a shared method
                // Rewrite to self.getChipmunkClass().symbol

                AstNode getClassCallNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, index, line, column));
                AstNode selfDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));
                AstNode varDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));

                AstNode self = new AstNode(NodeType.ID, new Token("self", TokenType.IDENTIFIER, index, line, column));

                AstNode getClass = new AstNode(NodeType.ID, new Token("getChipmunkClass", TokenType.IDENTIFIER, index, line, column));

                selfDotNode.addChild(self);
                selfDotNode.addChild(getClass);

                getClassCallNode.addChild(selfDotNode);

                varDotNode.addChild(getClassCallNode);
                varDotNode.addChild(child);

                return varDotNode;
            } else {
                // Symbol is an instance field
                // Rewrite to self.symbol

                AstNode selfDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));

                AstNode self = new AstNode(NodeType.ID, new Token("self", TokenType.IDENTIFIER, index, line, column));
                selfDotNode.addChild(self);
                selfDotNode.addChild(child);

                return selfDotNode;
            }

        }
        return child;
    }

    protected boolean isMethodBindTarget(AstNode node, int index){
        if(index == 1 && node.is(NodeType.OPERATOR)){
            if(node.getToken().type().equals(TokenType.DOUBLECOLON)){
                return true;
            }
        }
        return false;
    }
}
