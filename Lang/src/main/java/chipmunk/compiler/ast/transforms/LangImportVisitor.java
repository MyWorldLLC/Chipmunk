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

package chipmunk.compiler.ast.transforms;

import chipmunk.compiler.ast.*;
import chipmunk.compiler.lexer.Token;
import chipmunk.compiler.lexer.TokenType;
import chipmunk.compiler.symbols.Symbol;
import chipmunk.modules.lang.LangModule;

import java.util.List;

public class LangImportVisitor implements AstVisitor {

    @Override
    public void visit(AstNode node) {
        if(node instanceof ModuleNode) {
            ModuleNode moduleNode = (ModuleNode) node;

            var langImport = Imports.makePlain(
                    new Token("import", TokenType.IMPORT),
                    new Token(LangModule.MODULE_NAME, TokenType.IDENTIFIER),
                    new Token("import", TokenType.IMPORT),
                    List.of(new Token("*", TokenType.STAR)));

            moduleNode.addChild(0, langImport);
        }
    }
}
