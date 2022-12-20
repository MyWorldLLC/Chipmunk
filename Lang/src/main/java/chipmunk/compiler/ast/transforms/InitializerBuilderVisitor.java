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
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.TokenType;

import java.util.*;
import java.util.stream.Collectors;

public class InitializerBuilderVisitor implements AstVisitor {

    protected Deque<AstNode> modulesAndClasses = new ArrayDeque<>();

    @Override
    public void visit(AstNode node) {

        if(node instanceof ModuleNode){

            ModuleNode moduleNode = (ModuleNode) node;

            MethodNode initializer = new MethodNode("$module_init$");
            initializer.addParam(new VarDecNode("vm"));
            moduleNode.getChildren().add(0, initializer);

            // Create imported module fields & generate vm calls to initialize them
            List<AstNode> imports = moduleNode.getChildren()
                    .stream()
                    .filter(n -> n.is(NodeType.IMPORT))
                    .collect(Collectors.toList());

            Set<String> alreadyImported = new HashSet<>();

            for(AstNode im : imports){

                final int index = im.getTokenIndex();
                final int line = im.getLineNumber();
                final int column = 0;

                final String moduleName = Imports.getModule(im).getName();

                if(alreadyImported.contains(moduleName)){
                    continue;
                }

                VarDecNode dec = new VarDecNode(ChipmunkCompiler.importedModuleName(moduleName));

                AstNode getModuleCallNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, index, line, column));
                AstNode vmDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));
                vmDotNode.getChildren().add(new AstNode(NodeType.ID, new Token("vm", TokenType.IDENTIFIER)));
                vmDotNode.getChildren().add(new AstNode(NodeType.ID, new Token("getModule", TokenType.IDENTIFIER)));

                getModuleCallNode.getChildren().add(vmDotNode);
                getModuleCallNode.getChildren().add(new AstNode(NodeType.LITERAL, new Token("\"" + moduleName + "\"", TokenType.STRINGLITERAL)));

                dec.setAssignExpr(getModuleCallNode);
                moduleNode.getChildren().add(dec);

                alreadyImported.add(moduleName);
            }

            modulesAndClasses.push(moduleNode);

            node.visitChildren(this);

            modulesAndClasses.pop();

        }else if(node.is(NodeType.CLASS)){

            MethodNode sharedInitializer = new MethodNode("$class_init$");
            sharedInitializer.getSymbol().setShared(true);
            node.getChildren().add(0, sharedInitializer);

            MethodNode instanceInitializer = new MethodNode("$instance_init$");
            node.getChildren().add(1, instanceInitializer);

            modulesAndClasses.push(node);

            node.visitChildren(this);

            modulesAndClasses.pop();
        }else if(node instanceof VarDecNode){

            VarDecNode varDec = (VarDecNode) node;
            AstNode assignExpression = varDec.getAssignExpr();

            // Rewrite empty assign expression to null assignment
            if(assignExpression == null){
                assignExpression = new AstNode(NodeType.LITERAL, new Token("null", TokenType.NULL));
            }

            AstNode owner = modulesAndClasses.peek();

            AstNode id = new AstNode(NodeType.ID, varDec.getIDNode().getToken());

            AstNode assignStatement = new AstNode(NodeType.OPERATOR, new Token("=", TokenType.EQUALS));
            assignStatement.getChildren().add(id);
            assignStatement.getChildren().add(assignExpression);

            varDec.setAssignExpr(null);

            if (owner instanceof ModuleNode) {
                ((MethodNode) owner.getChildren().get(0)).addToBody(assignStatement);
            } else if (owner.is(NodeType.CLASS)) {
                if (varDec.getSymbol().isShared()) {
                    ((MethodNode) owner.getChildren().get(0)).addToBody(assignStatement);
                } else {
                    ((MethodNode) owner.getChildren().get(1)).addToBody(assignStatement);
                }
            }


        }


    }

}
