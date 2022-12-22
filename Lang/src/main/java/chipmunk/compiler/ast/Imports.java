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
import chipmunk.compiler.symbols.Symbol;
import chipmunk.util.Require;

import java.util.List;

public class Imports {

    public static AstNode makePlain(Token root, Token moduleName, Token symbolRoot, List<Token> symbols){
        var node = new AstNode(NodeType.IMPORT, root);

        setModule(node, moduleName);
        addImportSymbols(node, symbolRoot, symbols);

        return node;
    }

    public static AstNode makeAliased(Token root, Token moduleName, Token symbolRoot, List<Token> symbols, Token aliasRoot, List<Token> aliases){
        var node = makePlain(root, moduleName, symbolRoot, symbols);
        addImportSymbols(node, aliasRoot, aliases);

        return node;
    }

    public static boolean isImportAll(AstNode n){
        Require.require(n.is(NodeType.IMPORT), "%s is not an import node", n.getType());
        return n.childCount() > 1 && n.getChild(1).getChild(0).getSymbol().getName().equals("*");
    }

    public static void setModule(AstNode n, Token moduleName){
        var node = new AstNode(NodeType.ID, moduleName);
        node.setSymbol(new Symbol(moduleName.text()));
        n.addChild(0, node);
    }

    public static Symbol getModule(AstNode n){
        Require.require(n.is(NodeType.IMPORT), "%s is not an import node", n.getType());
        return n.getChild(0).getSymbol();
    }

    public static List<Symbol> symbols(AstNode n){
        //Require.require(!isImportAll(n), "No symbols are imported from an import * node.");
        return n.childCount() > 1 ? childSymbols(n.getChild(1)) : List.of();
    }

    public static List<Symbol> aliases(AstNode n){
        Require.require(!isImportAll(n), "No symbols are aliased from an import * node.");
        Require.require(isAliased(n), "Node does not define aliases");
        return n.childCount() == 3 ? childSymbols(n.getChild(2)) : List.of();
    }

    public static boolean isAliased(AstNode n){
        Require.require(n.is(NodeType.IMPORT), "%s is not an import node", n.getType());
        return n.childCount() == 3;
    }

    public static boolean verifyAliasCount(AstNode n){
        return symbols(n).size() == aliases(n).size();
    }

    protected static List<Symbol> childSymbols(AstNode n){
        Require.require(
                n.getChildren().stream().allMatch(c -> c.is(NodeType.ID)),
                "Import symbols are malformed. This is a compiler bug.");

        return n.getChildren().stream().map(AstNode::getSymbol).toList();
    }

    public static void addImportSymbols(AstNode n, Token symbolRoot, List<Token> symbols){
        var symbolNode = new AstNode(NodeType.IMPORT, symbolRoot);
        n.addChild(symbolNode);

        symbols.forEach(t -> {
            var symbol = new AstNode(NodeType.ID, t);
            symbol.setSymbol(new Symbol(t.text()));
            symbolNode.addChild(symbol);
        });
    }
}
