/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.compiler.ir;

import chipmunk.compiler.Import;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.ir.blocks.*;
import chipmunk.compiler.ir.expression.*;
import chipmunk.compiler.ir.flow.BreakNode;
import chipmunk.compiler.ir.flow.ContinueNode;
import chipmunk.compiler.ir.flow.ReturnNode;
import chipmunk.compiler.ir.flow.ThrowNode;
import chipmunk.compiler.ir.passes.EvaluationEnvironment;
import chipmunk.compiler.lexer.ChipmunkLexer;
import chipmunk.compiler.parser.parselets.LiteralParselet;
import chipmunk.compiler.types.*;

import java.util.ArrayList;

public class IRBuilder {

    public ModuleNode buildModule(EvaluationEnvironment env, AstNode module){
        var type = new ModuleType(Modules.getName(module).getName());
        var irNode = new ModuleNode(type);
        populateDebug(irNode, module);

        for(var element : module.getChildren()){
            switch (element.getNodeType()){
                case IMPORT -> {
                    var importingFrom = Imports.getModule(element).getName();
                    if(Imports.isImportAll(element)){
                        env.resolveImport(importingFrom)
                                .ifPresentOrElse(symbols -> {
                                    for(var symbol : symbols){
                                        irNode.moduleType().imports().declare(new Import(symbol, importingFrom));
                                    }
                                }, () -> env.error("Could not find module " + importingFrom));
                    }else{
                        var symbols = Imports.symbols(element);
                        var aliases = Imports.isAliased(element) ? Imports.aliases(element) : null;
                        for(int i = 0; i < symbols.size(); ++i){
                            var symbol = symbols.get(i).getName();
                            var resolveSymbol = (aliases != null ? aliases.get(i) : symbols.get(i)).getName();
                            env.resolveImport(importingFrom, resolveSymbol)
                                    .ifPresentOrElse(resolved -> {
                                        if(aliases != null){
                                            irNode.moduleType().imports().declare(new Import(symbol, importingFrom, resolved));
                                        }else{
                                            irNode.moduleType().imports().declare(new Import(symbol, importingFrom));
                                        }
                                    }, () -> env.error("Could not find %s.%s", importingFrom, resolveSymbol));
                        }
                    }
                }
                case VAR_DEC -> irNode.addChild(buildVarDec(env, irNode, element));
                case CLASS -> irNode.addChild(buildClass(env, irNode, type, element));
                case METHOD -> irNode.addChild(buildMethod(env, irNode, element));
                case COMMENT -> {} // Comment is a no-op for now
                default -> env.error("Invalid module element: " + element.getNodeType());
            }
        }

        return irNode;
    }

    public VarDecNode buildVarDec(EvaluationEnvironment env, ParentNode parent, AstNode varDec){
        var dec = new VarDecNode(VarDec.getVarName(varDec), parent);
        dec.declaredType(getDeclaredType(varDec));

        populateDebug(dec, varDec);
        if(VarDec.hasAssignment(varDec)){
            dec.addChild(buildExpression(env, dec, VarDec.getAssignment(varDec)));
        }
        return dec;
    }

    public ClassNode buildClass(EvaluationEnvironment env, ParentNode parent, BaseClassType parentType, AstNode cls){
        var type = new ClassType(cls.getSymbol().getName(), parentType);
        var irNode = new ClassNode(type, parent);
        populateDebug(irNode, cls);

        for(var element : cls.getChildren()) {
            switch (element.getNodeType()) {
                case VAR_DEC -> irNode.addChild(buildVarDec(env, irNode, element));
                case CLASS -> irNode.addChild(buildClass(env, irNode, type, element));
                case METHOD -> irNode.addChild(buildMethod(env, irNode, element));
                case COMMENT -> {} // Comment is a no-op for now
                default -> env.error("Invalid class element: " + element.getNodeType());
            }
        }

        return irNode;
    }

    public MethodNode buildMethod(EvaluationEnvironment env, ParentNode parent, AstNode method){

        var pTypes = new ArrayList<ObjectType>();
        var pNames = new ArrayList<String>();

        Methods.visitParams(method, param -> {
            var name = param.getSymbol().getName();
            pNames.add(name);
            pTypes.add(getDeclaredType(param));
        });
        var rType = getDeclaredType(method);
        var type = new MethodType(rType, pTypes, pNames);

        var irNode = new MethodNode(Methods.getName(method).getName(), parent, type);
        populateDebug(irNode, method);

        Methods.visitBody(method, statement -> appendStatementToBlockBody(env, irNode, statement));

        return irNode;
    }

    public IRNode buildFlowControl(EvaluationEnvironment env, ParentNode parent, AstNode flowControl){
        return switch (flowControl.getToken().type()){
            case BREAK -> new BreakNode(parent);
            case CONTINUE -> new ContinueNode(parent);
            case RETURN -> {
                var rNode = new ReturnNode(parent);
                if(flowControl.hasChildren()){
                    rNode.addChild(buildExpression(env, rNode, flowControl.getChild()));
                }
                yield rNode;
            }
            case THROW -> {
                var tNode = new ThrowNode(parent);
                tNode.addChild(buildExpression(env, tNode, flowControl.getChild()));
                yield tNode;
            }
            default -> throw new IllegalArgumentException("Invalid flow control statement: " + flowControl.getToken().type());
        };
    }

    public IRNode buildWhileLoop(EvaluationEnvironment env, LocalBlockNode parent, AstNode loop){
        var irNode = new WhileNode(parent);
        irNode.addChild(buildExpression(env, irNode, loop.getChild()));
        loop.visitChildren(statement -> appendStatementToBlockBody(env, irNode, statement), 1);
        return irNode;
    }

    public IRNode buildForLoop(EvaluationEnvironment env, LocalBlockNode parent, AstNode loop){
        var irNode = new ForNode(parent);
        irNode.addChild(buildExpression(env, irNode, loop.getChild()));
        loop.visitChildren(statement -> appendStatementToBlockBody(env, irNode, statement), 1);
        return irNode;
    }

    public IRNode buildIfElse(EvaluationEnvironment env, LocalBlockNode parent, AstNode ifElse){
        var irNode = new IfElseNode(parent);
        for(int i = 0; i < ifElse.childCount(); i++){
            var branch = ifElse.getChild(i);
            if(i < ifElse.childCount() - 1){
                // If branches
                var block = new IfNode(irNode);
                block.addChild(buildExpression(env, block, branch.getChild()));
                branch.visitChildren(statement -> appendStatementToBlockBody(env, block, statement), 1);
                irNode.addChild(block);
            }else{
                // Else block
                var block = new ElseNode(irNode);
                branch.visit(statement -> appendStatementToBlockBody(env, block, statement));
                irNode.addChild(block);
            }
        }
        return irNode;
    }

    public IRNode buildTryCatch(EvaluationEnvironment env, LocalBlockNode parent, AstNode tryCatch){
        var irNode = new TryCatchNode(parent);
        var tryNode = new TryNode(irNode);
        tryCatch.getChild().visitChildren(statement -> appendStatementToBlockBody(env, tryNode, statement));
        if(tryCatch.childCount() > 1 && tryCatch.getChild(1).is(NodeType.CATCH)){
            var catchAst = tryCatch.getChild(1);
            var catchNode = new CatchNode(catchAst.getChild().getToken().text(), irNode);
            catchAst.visitChildren(statement -> appendStatementToBlockBody(env, catchNode, statement), 1);
            irNode.addChild(catchNode);
        }
        if(tryCatch.childCount() > 1 && tryCatch.getRight().is(NodeType.FINALLY)){
            var finallyNode = new FinallyNode(irNode);
            tryCatch.getRight().visitChildren(statement -> appendStatementToBlockBody(env, finallyNode, statement));
            irNode.addChild(finallyNode);
        }
        return irNode;
    }

    public void appendStatementToBlockBody(EvaluationEnvironment env, LocalBlockNode parent, AstNode statement){
        switch (statement.getNodeType()){
            case VAR_DEC -> parent.addChild(buildVarDec(env, parent, statement));
            case FLOW_CONTROL -> parent.addChild(buildFlowControl(env, parent, statement));
            case WHILE -> parent.addChild(buildWhileLoop(env, parent, statement));
            case FOR -> parent.addChild(buildForLoop(env, parent, statement));
            case IF_ELSE -> parent.addChild(buildIfElse(env, parent, statement));
            case TRY_CATCH -> parent.addChild(buildTryCatch(env, parent, statement));
            case OPERATOR -> parent.addChild(buildExpression(env, parent, statement));
            case COMMENT -> {} // Comment is a no-op for now
            default -> env.error(parent, "Invalid statement: " + statement.getNodeType());
        }
    }

    public ExpressionNode buildExpression(EvaluationEnvironment env, ParentNode parent, AstNode exp){
        return switch (exp.getNodeType()){
            case ID -> new IdNode(exp.getToken().text(), parent);
            case LITERAL -> {
                yield switch(exp.getToken().type()){
                    case BOOLLITERAL -> new LiteralNode(Boolean.parseBoolean(exp.getToken().text()), BuiltinTypes.BOOLEAN, parent);
                    case BINARYLITERAL, OCTLITERAL, HEXLITERAL, INTLITERAL -> {
                        var literal = exp.getToken().text().replace("_", "");
                        var radix = LiteralParselet.radix(literal);
                        var type = LiteralParselet.intTypeOf(literal);
                        literal = LiteralParselet.stripQualifier(LiteralParselet.stripRadixQualifier(literal));
                        yield switch (type.bitSize()){
                            case 8 -> new LiteralNode(Byte.parseByte(literal, radix), BuiltinTypes.BYTE, parent);
                            case 16 ->  new LiteralNode(Short.parseShort(literal, radix), BuiltinTypes.SHORT, parent);
                            case 32 ->  new LiteralNode(Integer.parseInt(literal, radix), BuiltinTypes.INT, parent);
                            case 64 ->  new LiteralNode(Long.parseLong(literal, radix), BuiltinTypes.LONG, parent);
                            default -> throw new IllegalArgumentException("Invalid bitsize for integer literal: " + type.bitSize() + ". This is a compiler bug.");
                        };
                    }
                    case FLOATLITERAL -> {
                        var stripped = LiteralParselet.stripQualifier(exp.getToken().text());
                        var type = LiteralParselet.floatTypeOf(exp.getToken().text());
                        yield switch (type.bitSize()){
                            case 32 -> new LiteralNode(Float.parseFloat(stripped), BuiltinTypes.FLOAT, parent);
                            case 64 -> new LiteralNode(Double.parseDouble(stripped), BuiltinTypes.DOUBLE, parent);
                            default -> throw new IllegalArgumentException("Invalid bitsize for float literal: " + type.bitSize() + ". This is a compiler bug.");
                        };
                    }
                    case STRINGLITERAL -> {
                        // strip quotes
                        String value = ChipmunkLexer.unescapeString(exp.getToken().text().substring(1, exp.getToken().text().length() - 1));
                        yield new LiteralNode(value, BuiltinTypes.STRING, parent);
                    }
                    case NULL -> new LiteralNode(null, BuiltinTypes.ANY, parent);
                    default -> throw new IllegalArgumentException("Invalid literal type: " + exp.getToken().type() + ". This is a compiler bug.");
                };

            }
            case LIST -> {
                var list = new ListNode(parent);
                exp.visitChildren(child -> list.addChild(buildExpression(env, list, child)));
                yield list;
            }
            case MAP -> {
                var map = new MapNode(parent);
                exp.visitChildren(child -> {
                    map.addChild(buildExpression(env, map, child.getLeft()));
                    map.addChild(buildExpression(env, map, child.getRight()));
                });
                yield map;
            }
            case ITERATOR -> {
                var name = exp.getLeft().getToken().text();
                var it = new IteratorNode(name, parent);
                it.addChild(buildExpression(env, it, exp.getRight()));
                yield it;
            }
            case BINDING -> {
                var irNode = new OperationNode("::", parent);
                irNode.addChild(buildExpression(env, irNode, exp.getLeft()));
                irNode.addChild(buildExpression(env, irNode, exp.getRight()));
                yield irNode;
            }
            case OPERATOR -> {
                // TODO - check for & handle forms that don't neatly resolve to simple unary/binary operators (a.b(), a[b] = c, etc)
                var op = new OperationNode(exp.getToken().text(), parent);
                exp.visitChildren(child -> op.addChild(buildExpression(env, op, child)));
                yield op;
            }
            default -> throw new IllegalArgumentException("Invalid expression node: " + exp.getNodeType() + ". This is a compiler bug.");
        };
    }

    public ObjectType getDeclaredType(AstNode ast){
        var typeName = ast.getResultTypeName();
        return typeName != null ? new UnresolvedType(typeName) : BuiltinTypes.ANY;
    }

    public void populateDebug(IRNode ir, AstNode ast){
        ir.debugInfo(ast.getToken().line(), ast.getToken().column());
    }

}
