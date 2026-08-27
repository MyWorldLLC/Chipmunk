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

import chipmunk.compiler.CompilerUtil;
import chipmunk.compiler.ast.*;

import java.util.*;

public class InitializerBuilderVisitor implements AstVisitor {


    @Override
    public void visit(AstNode module) {

        if(module.is(NodeType.MODULE)){

            AstNode initializer = Methods.make("$module_init$");
            Methods.addParam(initializer, Identifier.make("vm"));
            module.addChild(0, initializer);

            // Create imported module fields & generate vm calls to initialize them
            List<AstNode> imports = module.getChildren()
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
                var importName = CompilerUtil.importedModuleName(moduleName);
                AstNode dec = VarDec.makeImplicit(importName);

                module.addChild(0, dec);
                initializer.addChild(Operators.make("=",
                        Identifier.make(importName),
                        Methods.makeInvocation(Identifier.make("vm"), "getModule", line, Literals.makeString("\"" + moduleName + "\""))));

                alreadyImported.add(moduleName);
            }

            module.visitChildren(this);

        }else if(module.is(NodeType.CLASS)){

            AstNode sharedInitializer = Methods.make("$class_init$");
            sharedInitializer.getSymbol().setShared(true);
            module.addChild(0, sharedInitializer);

            AstNode instanceInitializer = Methods.make("$instance_init$");
            module.addChild(1, instanceInitializer);

            module.visitChildren(this);
        }
    }

}
