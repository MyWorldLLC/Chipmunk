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

package chipmunk.compiler.codegen.lua;

import chipmunk.binary.*;
import chipmunk.compiler.ast.*;
import chipmunk.compiler.codegen.ClassVisitor;
import chipmunk.compiler.codegen.Codegen;
import chipmunk.compiler.codegen.MethodVisitor;
import chipmunk.compiler.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;

public class ModuleVisitor implements AstVisitor {

	protected String fileName;
	protected BinaryModule module;
	protected Codegen initCodegen;
	protected LuaPrinter printer;

	protected List<Object> constantPool;
	protected List<BinaryImport> imports;
	protected BinaryNamespace namespace;
	
	public ModuleVisitor(String fileName){
		this.fileName = fileName;
		constantPool = new ArrayList<>();
		imports = new ArrayList<>();
		namespace = new BinaryNamespace();
	}

	@Override
	public void visit(AstNode node) {
		switch (node.getNodeType()){
			case MODULE -> {
				var name = node.getSymbol().getName();
				printer.emit("""
						local %s = {}
						""".formatted(name));
				node.getSymbolTable().setDebugSymbol(name);

				node.visitChildren(this);

				printer.emit("""
					return %s
					""".formatted(name));
			}
			case CLASS -> {
				ClassVisitor visitor = new ClassVisitor(constantPool, module);
				node.visit(visitor);

				BinaryClass cls = visitor.getBinaryClass();

				module.getNamespace().addEntry(BinaryNamespace.Entry.makeClass(cls.getName(), (byte)0, cls));
			}
			case METHOD -> {
				MethodVisitor visitor = new MethodVisitor(constantPool, module);
				node.visit(visitor);
				BinaryMethod method = visitor.getMethod();

				module.getNamespace().addEntry(
						BinaryNamespace.Entry.makeMethod(visitor.getMethodSymbol().getName(), (byte)0, method));

			}
			case IMPORT -> {
				boolean importAll = Imports.isImportAll(node);

				BinaryImport im = new BinaryImport(Imports.getModule(node).getName(), importAll);

				if(!importAll){
					im.setSymbols(Imports.symbols(node).stream().map(Symbol::getName).toList().toArray(new String[]{}));
					if(Imports.isAliased(node)){
						im.setAliases(Imports.aliases(node).stream().map(Symbol::getName).toList().toArray(new String[]{}));
					}
				}

				imports.add(im);
			}
			case VAR_DEC ->
				// At this point, if there was an in-line assignment for this it was already moved to an initializer
                // AST, so we don't need to do anything except add the method to the module namespace.
                // Initialize to nil.
                    printer.emit("""
                            local %s = nil
                            """.formatted(VarDec.getVarName(node)));
			default -> throw new IllegalArgumentException("Error parsing module " + module.getName() + ": illegal AST node type " + node.getClass());
		}
	}
	
	public BinaryModule getModule(){
		module.setConstantPool(constantPool.toArray());
		module.setImports(imports.toArray(new BinaryImport[]{}));
		
		return module;
	}

	public LuaPrinter getPrinter(){
		return printer;
	}

	public String getLuaModule(){
		return printer.toSource();
	}

}
