package chipmunk.modules;

import chipmunk.ChipmunkUtil;
import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.uuid.UUIDCreateRandom;
import chipmunk.modules.uuid.UUIDFromString;
import chipmunk.modules.uuid.UUIDToString;

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

        uuidModule.getNamespace().set("_randomUUID", new UUIDCreateRandom());
        uuidModule.getNamespace().set("_fromString", new UUIDFromString());
        uuidModule.getNamespace().set("_toString", new UUIDToString());

        return uuidModule;
    }
}
