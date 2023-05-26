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

package chipmunk.cli;

import chipmunk.binary.BinaryModule;
import chipmunk.binary.BinaryNamespace;
import chipmunk.binary.FieldType;
import chipmunk.cli.commands.Run;
import chipmunk.cli.commands.Test;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.ChipmunkSource;
import chipmunk.compiler.Compilation;
import chipmunk.modules.buffer.BufferModule;
import chipmunk.modules.imports.JvmImportModule;
import chipmunk.modules.math.MathModule;
import chipmunk.modules.system.SystemModule;
import chipmunk.pkg.Entrypoint;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.jvm.CompilationUnit;
import chipmunk.vm.locators.FileModuleLocator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;

@Command(name = "chipmunk",
        mixinStandardHelpOptions = true,
        version = "chipmunk 1.0-alpha",
        description = "Chipmunk VM CLI",
        subcommands = {
            Run.class,
            Test.class
        }
)
public class ChipmunkCLI implements Callable<Integer> {

    public static final String CHIPMUNK_SRC_EXTENSION = "chp";
    public static final String CHIPMUNK_BIN_EXTENSION = "chpb";

    @Override
    public Integer call(){
        return new Run().call();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ChipmunkCLI()).execute(args);
        System.exit(exitCode);
    }
}
