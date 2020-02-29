package chipmunk.modules.runtime;

import java.util.Collection;

public class ChipmunkModuleBuilder {

    public static CModule buildLangModule(){
        CModule lang = new CModule("chipmunk.lang");

        return lang;
    }

    public static CModule build(String name, Collection<? extends Object> contents){
        CModule module = new CModule(name);

        // TODO - reflectively build a module from the passed collection, according to the module construction rules

        return module;
    }
}
