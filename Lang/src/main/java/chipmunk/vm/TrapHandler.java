package chipmunk.vm;

import chipmunk.vm.jvm.TrapSite;

public interface TrapHandler {

    default void runtimeTrap(Object payload){}

    // TODO - replace this verbosity with dynamically computed constants
    default void backJump(TrapSite site){}
    default void methodCall(TrapSite site,
                            String targetCls, String targetMethodName, String targetMethodSignature){}

    default void arrayAlloc(TrapSite site, int dimensions, int capacity){}

    default void objectAlloc(TrapSite site, String targetCls, String targetConstructorSignature){}

}
