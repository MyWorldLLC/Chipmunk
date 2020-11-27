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
import chipmunk.binary.BinaryReader;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.ChipmunkSource;
import chipmunk.compiler.Compilation;
import chipmunk.modules.buffer.BufferModule;
import chipmunk.modules.imports.ImportModule;
import chipmunk.modules.math.MathModule;
import chipmunk.modules.system.SystemModule;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;
import chipmunk.vm.ModuleLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "chipmunk", mixinStandardHelpOptions = true, version = "chipmunk 1.0-alpha",
        description = "Chipmunk VM CLI")
public class ChipmunkCLI implements Callable<Integer> {

    public static final String CHIPMUNK_SRC_EXTENSION = "chp";
    public static final String CHIPMUNK_BIN_EXTENSION = "chpb";

    protected static final String NO_SOURCE = "";

    @Parameters(index = "0", defaultValue = NO_SOURCE, description = "Chipmunk source or bytecode to run")
    protected String codePath;

    @Parameters(index = "1..*", defaultValue = "", description = "Arguments to Chipmunk program")
    protected String[] args;

    public void registerBuiltins(ModuleLoader loader) {
        // Register all builtin Chipmunk & Native modules
        loader.registerNativeFactory(SystemModule.SYSTEM_MODULE_NAME, () -> new SystemModule(args));
        loader.registerNativeFactory(ImportModule.IMPORT_MODULE_NAME, ImportModule::new);
        loader.registerNativeFactory(BufferModule.BUFFER_MODULE_NAME, BufferModule::new);
        loader.registerNativeFactory(MathModule.MATH_MODULE_NAME, MathModule::new);
    }

    @Override
    public Integer call() {

        try {

            // TODO - add flags for dependency search path

            ChipmunkVM vm = new ChipmunkVM();

            ModuleLoader loader = new ModuleLoader();
            registerBuiltins(loader);

            // TODO - register locators for sources/binaries on the search path

            // If no script specified, read from System.in
            ChipmunkSource source = null;
            if (codePath.equals(NO_SOURCE)) {
                if(System.in.available() > 0){
                    source = new ChipmunkSource(System.in, "Script.chp");
                } else {
                    System.out.println("No source specified, exiting");
                    return 1;
                }
            } else {
                Path sourcePath = Paths.get(codePath);
                source = new ChipmunkSource(new BufferedInputStream(new FileInputStream(sourcePath.toFile())), sourcePath.getFileName().toString());
            }

            // Compile source & run
            BinaryModule[] modules = null;
            if(source.getFileName().endsWith(CHIPMUNK_SRC_EXTENSION)){
                Compilation compilation = new Compilation();
                compilation.addSource(source);

                ChipmunkCompiler compiler = new ChipmunkCompiler(loader);
                compiler.setModuleLoader(loader);
                modules = compiler.compile(compilation);
            }else if(source.getFileName().endsWith(CHIPMUNK_BIN_EXTENSION)){
                BinaryReader reader = new BinaryReader();
                modules = new BinaryModule[]{reader.readModule(source.getIs())};
            }else{
                System.out.println("Invalid file: extension must be .chp or .chpb");
                return 1;
            }
            loader.addToLoaded(Arrays.asList(modules));

            ChipmunkScript script = vm.compileScript(modules);
            script.setModuleLoader(loader);
            vm.runAsync(script).get();

            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ChipmunkCLI()).execute(args);
        System.exit(exitCode);
    }
}
