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

@Command(name = "chipmunk", mixinStandardHelpOptions = true, version = "chipmunk 1.0-alpha",
        description = "Chipmunk VM CLI")
public class ChipmunkCLI implements Callable<Integer> {

    public static final String CHIPMUNK_SRC_EXTENSION = "chp";
    public static final String CHIPMUNK_BIN_EXTENSION = "chpb";

    protected static final String NO_SOURCE = "";

    @Option(names = {"-e", "-entrypoint"}, description = "Entrypoint in the form module.name::method")
    protected String entryPoint;

    @Option(names = {"-s", "-srcdirs"}, defaultValue = "./", description = "Comma separated list of directories of sources to compile")
    protected String srcDirs;

    @Option(names = {"-b", "-binDirs"}, defaultValue = "./", description = "Comma separated list of directories to load binary modules from")
    protected String binDirs;

    @Parameters(index = "0", defaultValue = NO_SOURCE, description = "Chipmunk source or bytecode to run")
    protected String codePath;

    @Parameters(index = "1..*", defaultValue = "", description = "Arguments to Chipmunk program")
    protected String[] args;

    public void registerBuiltins(ModuleLoader loader) {
        // Register all builtin Chipmunk & Native modules
        loader.registerNativeFactory(SystemModule.SYSTEM_MODULE_NAME, () -> new SystemModule(args, System.getenv()));
        loader.registerNativeFactory(JvmImportModule.IMPORT_MODULE_NAME, JvmImportModule::new);
        loader.registerNativeFactory(BufferModule.BUFFER_MODULE_NAME, BufferModule::new);
        loader.registerNativeFactory(MathModule.MATH_MODULE_NAME, MathModule::new);
    }

    @Override
    public Integer call() {

        try {

            // TODO - support running module binaries

            ChipmunkVM vm = new ChipmunkVM();

            ModuleLoader loader = new ModuleLoader();
            registerBuiltins(loader);

            List<Path> sourcePaths = new ArrayList<>();
            List<ChipmunkSource> sources = new ArrayList<>();


            // If no script specified, read from System.in
            if (codePath.equals(NO_SOURCE)) {
                if(System.in.available() > 0){
                    sources.add(new ChipmunkSource(System.in, "Script.chp"));
                }
            } else {
                if(!codePath.endsWith(CHIPMUNK_SRC_EXTENSION)){
                    System.out.println("Invalid file: extension must be .chp");
                    return 1;
                }

                Path sourcePath = Paths.get(codePath);
                if(Files.isDirectory(sourcePath)){
                    System.out.println(codePath + " is a directory");
                    return 1;
                }
                // Find all source files in the directory containing the source
                sourcePaths.add(sourcePath);
                List<Path> auxilaryPaths = CLIUtil.collectSources(sourcePath.toAbsolutePath().getParent());
                Iterator<Path> it = auxilaryPaths.iterator();
                while(it.hasNext()){
                    Path p = it.next();
                    if(Files.isSameFile(p, sourcePath)){
                        it.remove();
                        break;
                    }
                }
                sourcePaths.addAll(auxilaryPaths);

            }

            for(String src : srcDirs.split(",")){
                Path path = Paths.get(src);
                sourcePaths.addAll(CLIUtil.collectSources(path));
            }

            FileModuleLocator locator = new FileModuleLocator();
            for(String s : binDirs.split(",")){
                Path path = Paths.get(s);
                locator.getPaths().addAll(CLIUtil.collectSubDirs(path));
            }
            loader.addLocator(locator);

            for(Path sourcePath : sourcePaths){
                sources.add(new ChipmunkSource(Files.newInputStream(sourcePath), sourcePath.getFileName().toString()));
            }

            if(sources.size() == 0){
                System.out.println("No source specified, exiting");
                return 1;
            }

            // Compile source & run
            Compilation compilation = new Compilation();
            compilation.getSources().addAll(sources);

            ChipmunkCompiler compiler = new ChipmunkCompiler(loader);
            compiler.setModuleLoader(loader);
            BinaryModule[] modules  = compiler.compile(compilation);

            loader.addToLoaded(Arrays.asList(modules));

            CompilationUnit unit = new CompilationUnit();
            unit.setModuleLoader(loader);
            unit.setEntryModule("main");
            unit.setEntryMethodName("main");

            if(entryPoint != null){
                Entrypoint newEntrypoint = Entrypoint.fromString(entryPoint);
                unit.setEntryModule(newEntrypoint.getModule());
                unit.setEntryMethodName(newEntrypoint.getMethod());
            }else{
                // Verify default entrypoint is findable, search compiled modules for
                // main module if not
                BinaryModule mainModule = loader.loadBinary(unit.getEntryModule());
                if(mainModule == null || !(mainModule.getNamespace().has("main") && mainModule.getNamespace().getEntry("main").getType() == FieldType.METHOD)){
                    for(BinaryModule module : modules){
                        BinaryNamespace.Entry entry = module.getNamespace().getEntry("main");
                        if(entry != null && entry.getType() == FieldType.METHOD){
                            unit.setEntryModule(module.getName());
                        }
                    }
                }
            }

            ChipmunkScript script = vm.compileScript(unit);
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
