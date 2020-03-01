package chipmunk.modules;

import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.runtime.CModule;

import java.util.Collection;

public class ChipmunkModuleBuilder {

    public static CModule buildLangModule(){
        CModule lang = new CModule("chipmunk.lang");

        return lang;
    }

    public static CModule buildBufferModule() {
        CModule module = new CModule("chipmunk.buffer");
        module.getNamespace().set("Buffer", new BufferCClass(module));
        return module;
    }
}
