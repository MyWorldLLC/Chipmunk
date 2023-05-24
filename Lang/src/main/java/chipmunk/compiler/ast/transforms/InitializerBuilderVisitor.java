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

        if(node.is(NodeType.MODULE)){

            AstNode initializer = Methods.make("$module_init$");
            Methods.addParam(initializer, VarDec.makeImplicit("vm"));
            node.addChild(0, initializer);

            // Create imported module fields & generate vm calls to initialize them
            List<AstNode> imports = node.getChildren()
                    .stream()
                    .filter(n -> n.is(NodeType.IMPORT))
                    .toList();

            Set<String> alreadyImported = new HashSet<>();

            for(AstNode im : imports){

                final int line = im.getLineNumber();

                final String moduleName = Imports.getModule(im).getName();

                if(alreadyImported.contains(moduleName)){
                    continue;
                }

                // Create a module field named $imported_module_name & assign it to the retrieved module
                AstNode dec = VarDec.makeImplicit(ChipmunkCompiler.importedModuleName(moduleName));

                var getModuleCallNode = Methods.makeInvocation(Identifier.make("vm"), "getModule", line, Literals.makeString("\"" + moduleName + "\""));

                /*AstNode getModuleCallNode = new AstNode(NodeType.OPERATOR, new Token("(", TokenType.LPAREN, index, line, column));
                AstNode vmDotNode = new AstNode(NodeType.OPERATOR, new Token(".", TokenType.DOT, index, line, column));
                vmDotNode.addChild(Identifier.make("vm"));
                vmDotNode.addChild(Identifier.make("getModule"));

                getModuleCallNode.addChild(vmDotNode);
                getModuleCallNode.addChild(new AstNode(NodeType.LITERAL, new Token("\"" + moduleName + "\"", TokenType.STRINGLITERAL)));
*/
                VarDec.setAssignment(dec, getModuleCallNode);

                node.addChild(0, dec);

                alreadyImported.add(moduleName);
            }

            modulesAndClasses.push(node);

            node.visitChildren(this);

            modulesAndClasses.pop();

        }else if(node.is(NodeType.CLASS)){

            AstNode sharedInitializer = Methods.make("$class_init$");
            sharedInitializer.getSymbol().setShared(true);
            node.addChild(0, sharedInitializer);

            AstNode instanceInitializer = Methods.make("$instance_init$");
            node.addChild(1, instanceInitializer);

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
            assignStatement.addChild(id);
            assignStatement.addChild(assignExpression);

            VarDec.removeAssignment(node);

            if (owner.is(NodeType.MODULE)) {
                var initializer = owner.getChild(n -> Methods.isMethodNamed(n, "$module_init$"));
                // Sort assignments to '$' fields (imported modules) to the front of the initializer,
                // so that the writes happen before any potential reads of those fields in the initializer.
                if(VarDec.getVarName(node).startsWith("$")){
                    Methods.addToBody(initializer,0, assignStatement);
                }else{
                    Methods.addToBody(initializer, assignStatement);
                }
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
