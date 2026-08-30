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

package chipmunk.compiler.ir.passes;

import chipmunk.compiler.Compilation;
import chipmunk.compiler.imports.ImportResolver;
import chipmunk.compiler.ir.IRNode;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.compiler.types.ObjectType;

import java.util.*;

public class EvaluationEnvironment {

    protected final Compilation compilation;
    protected final List<ImportResolver> importResolvers;
    protected final List<String> errors;
    protected final List<String> warnings;

    public EvaluationEnvironment(Compilation compilation) {
        this.compilation = compilation;
        importResolvers = new ArrayList<>();
        errors = new ArrayList<>();
        warnings = new ArrayList<>();
    }

    public Compilation compilation() {
        return compilation;
    }

    public EvaluationEnvironment withResolvers(ImportResolver... resolvers){
        importResolvers.addAll(Arrays.asList(resolvers));
        return this;
    }

    public List<ImportResolver> resolvers(){
        return Collections.unmodifiableList(importResolvers);
    }

    public Optional<String> resolveImport(String module, String symbol){
        return importResolvers.stream().map(resolver -> resolver.resolve(module, symbol))
                .filter(Objects::nonNull)
                .map(Symbol::getName)
                .findFirst();
    }

    public Optional<List<String>> resolveImport(String module){
        return importResolvers.stream().map(resolver -> resolver.resolveSymbols(module))
                .filter(Objects::nonNull)
                .map(symbols -> symbols.stream().map(Symbol::getName).toList())
                .findFirst();
    }

    public void error(IRNode node, String message, Object... fmtArgs){
        var module = node.getModule();
        errors.add(String.format("%s %d:%d: %s", module.fileName(), node.lineNumber(), node.columnNumber(), String.format(message, fmtArgs)));
    }

    public void error(String message, Object... fmtArgs){
        errors.add(String.format(message, fmtArgs));
    }

    public void warning(IRNode node, String message, Object... fmtArgs){
        var module = node.getModule();
        warnings.add(String.format("%s %d:%d: %s", module.fileName(), node.lineNumber(), node.columnNumber(), String.format(message, fmtArgs)));
    }

    public void warning(String message, Object... fmtArgs){
        warnings.add(String.format(message, fmtArgs));
    }

    public List<String> errors(){
        return Collections.unmodifiableList(errors);
    }

    public List<String> warnings(){
        return Collections.unmodifiableList(warnings);
    }

    public boolean typeConflict(ObjectType actual, ObjectType expected) {
        return !actual.isAssignableTo(expected) && !actual.canPromoteTo(expected);
    }
}
