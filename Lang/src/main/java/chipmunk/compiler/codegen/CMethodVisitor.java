/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.compiler.codegen;

import chipmunk.compiler.assembler.ChipmunkAssembler;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.Methods;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.symbols.SymbolTable;
import chipmunk.runtime.CMethod;
import chipmunk.runtime.CModule;
import chipmunk.vm.tree.nodes.FunctionNode;

import java.util.Comparator;
import java.util.List;

import static chipmunk.compiler.ast.NodeType.*;
import static chipmunk.compiler.ast.NodeType.TRY_CATCH;

public class CMethodVisitor  implements AstVisitor {

    protected CMethod method;
    protected ChipmunkAssembler assembler;
    protected Codegen outerCodegen;
    protected SymbolTable symbols;
    protected Codegen codegen;
    protected AstNode methodNode;

    protected CModule module;

    protected boolean defaultReturn;

    protected boolean isInner;


    public CMethodVisitor(CModule module){
        defaultReturn = true;
        isInner = false;
        this.module = module;
    }

    public static CMethodVisitor innerMethodVisitor(Codegen outerCodegen, CModule module){
        var visitor = new CMethodVisitor(module);
        visitor.isInner = true;
        visitor.outerCodegen = outerCodegen;
        return visitor;
    }

    @Override
    public void visit(AstNode node) {

        method = new CMethod();

        if(node.is(METHOD)){
            methodNode = node;

            method.argCount = Methods.getParamCount(node);

            symbols = methodNode.getSymbolTable();
            symbols.sortSymbols(Comparator.comparingInt(s -> {
                if(s.getName().equals("self")){
                    return Integer.MIN_VALUE; // self is always the target of a bound method, and will never be an upvalue
                }else if(s.isUpvalueRef()){
                    return 1; // sort upvalue refs to the tail of the parameter list
                }else{
                    return -1; // normal parameters go between self & the upvalue refs
                }
            }));
            symbols.setDebugSymbol(node.getSymbol().getName());

            codegen = new Codegen(assembler, symbols, module);

            ExpressionStatementVisitor expStatVisitor = new ExpressionStatementVisitor(codegen);

            codegen.setVisitorForNode(OPERATOR, expStatVisitor);
            codegen.setVisitorForNode(ID, new NoOpVisitor()); // Handle id nodes that are on their own lines
            codegen.setVisitorForNode(METHOD, CMethodVisitor.innerMethodVisitor(codegen, module));
            //codegen.setVisitorForNode(ClassNode.class, new ClassVisitor(assembler.getConstantPool(), module, assembler));
            //codegen.setVisitorForNode(VAR_DEC, new VarDecVisitor(codegen));
            codegen.setVisitorForNode(IF_ELSE, new IfElseVisitor(codegen));
            codegen.setVisitorForNode(WHILE, new WhileVisitor(codegen));
            codegen.setVisitorForNode(FOR, new ForVisitor(codegen));
            codegen.setVisitorForNode(FLOW_CONTROL, new FlowControlVisitor(codegen));
            codegen.setVisitorForNode(TRY_CATCH, new TryCatchVisitor(codegen));

            codegen.enterScope(symbols, Methods.getParamCount(node));
            if(Methods.getBodyNodeCount(methodNode) == 1
                    && ExpressionVisitor.isExpressionNode(methodNode.getChild(methodNode.childCount() - 1))) {
                // this supports "lambda" methods - single expression methods that automatically return without the "return" keyword

                ExpressionVisitor visitor = new ExpressionVisitor(codegen);
                var code = visitor.visit(Methods.getLambdaBody(methodNode));
                method.code = new FunctionNode(code);
            }else {
                // regular methods
                Methods.visitBody(methodNode, codegen);
            }
            codegen.exitScope();

            if(defaultReturn){
                // return null in case a return has not yet been hit
                genDefaultReturn();
            }

        }

    }

    public void genSelfReturn(){
        //assembler.getLocal(0);
        //assembler._return();
    }

    public void genDefaultReturn(){
        //assembler.pushNull();
        //assembler._return();
    }

    public boolean willGenDefaultReturn(){
        return defaultReturn;
    }

    public void setDefaultReturn(boolean defaultReturn){
        this.defaultReturn = defaultReturn;
    }

    public CMethod getMethod(){

        //method.setCode(assembler.getCodeSegment());
        //method.setLocalCount(symbols.getLocalMax());
        //method.setModule(module);
        //method.setExceptionTable(codegen.getExceptionBlocks().toArray(new ExceptionBlock[]{}));
        //method.setDebugTable(codegen.getAssembler().getDebugTable().toArray(new DebugEntry[]{}));
        method.name = getMethodSymbol().getName();
        return method;
    }

    public Symbol getMethodSymbol(){
        return methodNode.getSymbol();
    }
}
