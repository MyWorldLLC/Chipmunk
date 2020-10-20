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

import chipmunk.compiler.Token;
import chipmunk.compiler.ast.*;

import java.util.ArrayDeque;
import java.util.Deque;

public class InitializerBuilderVisitor implements AstVisitor {

    protected Deque<BlockNode> modulesAndClasses = new ArrayDeque<>();

    @Override
    public void visit(AstNode node) {

        if(node instanceof ModuleNode){

            ModuleNode moduleNode = (ModuleNode) node;

            MethodNode initializer = new MethodNode("$module_init$");
            moduleNode.getChildren().add(0, initializer);

            modulesAndClasses.push(moduleNode);

            node.visitChildren(this);

            modulesAndClasses.pop();

        }else if(node instanceof ClassNode){
            ClassNode classNode = (ClassNode) node;

            MethodNode sharedInitializer = new MethodNode("$class_init$");
            classNode.getChildren().add(0, sharedInitializer);

            MethodNode instanceInitializer = new MethodNode("$instance_init$");
            classNode.getChildren().add(1, instanceInitializer);

            modulesAndClasses.push(classNode);

            node.visitChildren(this);

            modulesAndClasses.pop();
        }else if(node instanceof VarDecNode){

            VarDecNode varDec = (VarDecNode) node;
            AstNode assignExpression = varDec.getAssignExpr();

            if(assignExpression != null){
                BlockNode owner = modulesAndClasses.peek();

                IdNode id = new IdNode(varDec.getIDNode().getID());

                OperatorNode assignStatement = new OperatorNode(new Token("=", Token.Type.EQUALS));
                assignStatement.getChildren().add(id);
                assignStatement.getChildren().add(assignExpression);

                varDec.setAssignExpr(null);

                if(owner instanceof ModuleNode){
                    ((MethodNode) owner.getChildren().get(0)).addToBody(assignStatement);
                }else if(owner instanceof ClassNode){
                    if(varDec.getSymbol().isShared()){
                        ((MethodNode) owner.getChildren().get(0)).addToBody(assignStatement);
                    }else{
                        ((MethodNode) owner.getChildren().get(1)).addToBody(assignStatement);
                    }
                }
            }

        }


    }

}
