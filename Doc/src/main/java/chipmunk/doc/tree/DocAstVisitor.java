/*
 * Copyright (C) 2021 MyWorld, LLC
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

package chipmunk.doc.tree;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenStream;
import chipmunk.compiler.lexer.TokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a doc tree from a Chipmunk AST
 */
public class DocAstVisitor implements AstVisitor {

    protected TokenStream tokens;
    protected Deque<DocNode> docContext;

    protected List<DocNode> moduleRoots;

    public TokenStream getTokens(){
        return tokens;
    }

    public void setTokens(TokenStream tokens){
        this.tokens = tokens;
        docContext = new ArrayDeque<>();
        moduleRoots = new ArrayList<>();
    }

    public List<DocNode> getModuleRoots(){
        return moduleRoots;
    }

    @Override
    public void visit(AstNode node) {

        if(node instanceof ModuleNode){
            ModuleNode moduleNode = (ModuleNode) node;

            moduleRoots.add(enterDocNode(moduleNode));

            node.visitChildren(this);

            exitDocNode();

        }else if(node instanceof ClassNode){

            docContext.peek().getChildren().add(enterDocNode(node));

            node.visitChildren(this);

            exitDocNode();

        }else if(node instanceof VarDecNode || node instanceof MethodNode){

            docContext.peek().getChildren().add(enterDocNode(node));
            exitDocNode();

        }

    }

    protected DocNode enterDocNode(AstNode astNode){

        tokens.seek(astNode.getTokenIndex() - 1);

        Deque<Token> comments = new ArrayDeque<>();

        // Seek backwards, skipping newlines & accumulating doc comments until
        // we hit a token that's not a doc comment
        Token last = null;
        while(newlineOrDocComment(tokens.peek(-1)) && tokens.peek(-1) != last){
            Token token = tokens.peek(-1);
            tokens.rewind(1);
            if(token.type() == TokenType.COMMENT){
                comments.push(token);
            }
            last = token;
        }

        String lexicalName = ((SymbolNode) astNode).getSymbol().getName();

        String comment = comments.stream()
                .map(Token::text)
                .map(String::trim)
                .map(s -> s.substring(2))
                .collect(Collectors.joining(" "));

        DocNode docNode = new DocNode(comment, astNode, lexicalName);

        docContext.push(docNode);

        return docNode;
    }

    protected void exitDocNode(){
        docContext.pop();
    }

    protected boolean newlineOrDocComment(Token t){
        return t.type() == TokenType.NEWLINE ||
                (t.type() == TokenType.COMMENT && t.text().startsWith("##"));
    }

}
