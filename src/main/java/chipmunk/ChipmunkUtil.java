package chipmunk;

import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.modules.runtime.CModule;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChipmunkUtil {

    public static List<CModule> compileResources(String... resourcePaths) {
        List<CModule> modules = new ArrayList<>();

        ChipmunkCompiler compiler = new ChipmunkCompiler();
        for(String path : resourcePaths){
            InputStream is = ChipmunkUtil.class.getResourceAsStream(path);
            List<CModule> compiled = compiler.compile(is, Paths.get(path).getFileName().toString());
            modules.addAll(compiled);
        }

        return modules;
    }

    public static CModule getModule(String moduleName, Collection<CModule> modules){
        CModule namedModule = null;
        for(CModule module : modules){
            if(moduleName.equals(module.getName())){
                namedModule = module;
                break;
            }
        }
        return namedModule;
    }
}
