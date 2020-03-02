package chipmunk.modules;

import chipmunk.ChipmunkUtil;
import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.uuid.UUIDSupport;

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

    public static CModule buildUUIDModule() {
        Collection<CModule> modules = ChipmunkUtil.compileResources("/chipmunk/modules/chipmunk.uuid.chp");
        CModule uuidModule = ChipmunkUtil.getModule("chipmunk.uuid", modules);

        uuidModule.getNamespace().set("_randomUUID", UUIDSupport.createRandomUUID());
        uuidModule.getNamespace().set("_fromString", UUIDSupport.uuidFromString());
        uuidModule.getNamespace().set("_toString", UUIDSupport.uuidToString());

        return uuidModule;
    }
}
