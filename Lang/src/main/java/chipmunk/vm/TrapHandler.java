package chipmunk.vm;

import chipmunk.vm.jvm.MethodIdentifier;
import chipmunk.vm.jvm.TrapSite;

public interface TrapHandler {

    default void runtimeTrap(Object payload){}

    default void backJump(TrapSite site){}
    default void methodCall(TrapSite site, MethodIdentifier method){}

    default void arrayAlloc(TrapSite site, Class<?> arrayClass, int dimensions, int capacity){}

    default void objectAlloc(TrapSite site, Class<?> type){}

    default void objectInit(TrapSite site, Object object){

    }

}
