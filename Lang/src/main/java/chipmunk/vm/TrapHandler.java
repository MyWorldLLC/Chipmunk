package chipmunk.vm;

import chipmunk.vm.jvm.MethodIdentifier;
import chipmunk.vm.jvm.TrapSite;

public interface TrapHandler {

    default void runtimeTrap(Object payload){}

    default void backJump(TrapSite site){}
    default void methodCall(TrapSite site, MethodIdentifier method){}
    default Object postMethodCall(TrapSite site, MethodIdentifier method, Object result){
        return result;
    }

    default void arrayAlloc(TrapSite site, Class<?> arrayClass, int dimensions, int capacity){}
    default Object postArrayAlloc(TrapSite site, Object array){
        return array;
    }

    default void objectAlloc(TrapSite site, Class<?> type){}

    default Object postObjectInit(TrapSite site, Object object){
        return object;
    }

}
