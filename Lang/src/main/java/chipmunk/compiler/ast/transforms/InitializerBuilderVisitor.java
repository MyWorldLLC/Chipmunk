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

            AstNode initializer = Methods.make("$module_init$");
            Methods.addParam(initializer, VarDec.makeImplicit("vm"));
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

                AstNode dec = VarDec.makeImplicit(ChipmunkCompiler.importedModuleName(moduleName));

                AstNode getModuleCallNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, index, line, column));
                AstNode vmDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));
                vmDotNode.getChildren().add(Identifier.make("vm"));
                vmDotNode.getChildren().add(Identifier.make("getModule"));

                getModuleCallNode.getChildren().add(vmDotNode);
                getModuleCallNode.getChildren().add(new AstNode(NodeType.LITERAL, new Token("\"" + moduleName + "\"", TokenType.STRINGLITERAL)));

                VarDec.setAssignment(dec, getModuleCallNode);
                moduleNode.getChildren().add(dec);

                alreadyImported.add(moduleName);
            }

            modulesAndClasses.push(moduleNode);

            node.visitChildren(this);

            modulesAndClasses.pop();

        }else if(node.is(NodeType.CLASS)){

            AstNode sharedInitializer = Methods.make("$class_init$");
            sharedInitializer.getSymbol().setShared(true);
            node.getChildren().add(0, sharedInitializer);

            AstNode instanceInitializer = Methods.make("$instance_init$");
            node.getChildren().add(1, instanceInitializer);

            modulesAndClasses.push(node);

            node.visitChildren(this);

            modulesAndClasses.pop();
        }else if(node.is(NodeType.VAR_DEC)){

            AstNode assignExpression = VarDec.getAssignment(node);

            // Rewrite empty assign expression to null assignment
            if(assignExpression == null){
                assignExpression = new AstNode(NodeType.LITERAL, new Token("null", TokenType.NULL));
            }

            AstNode owner = modulesAndClasses.peek();

            AstNode id = new AstNode(NodeType.ID, VarDec.getIdentifier(node).getToken());

            AstNode assignStatement = new AstNode(NodeType.OPERATOR, new Token("=", TokenType.EQUALS));
            assignStatement.getChildren().add(id);
            assignStatement.getChildren().add(assignExpression);

            VarDec.removeAssignment(node);

            if (owner instanceof ModuleNode) {
                Methods.addToBody(owner.getChild(0), assignStatement);
            } else if (owner.is(NodeType.CLASS)) {
                if (node.getSymbol().isShared()) {
                    Methods.addToBody(owner.getChild(0), assignStatement);
                } else {
                    Methods.addToBody(owner.getChild(1), assignStatement);
                }
            }


        }


    }

}
