package chipmunk.vm;

public interface TrapHandler {

    default void runtimeTrap(Object payload){}

    // TODO - replace this verbosity with dynamically computed constants
    default void backJump(String cls, String method, int line){}
    default void methodCall(String cls, String method, int line,
                            String targetCls, String targetMethodName, String targetMethodSignature){}

    default void recursiveCall(String cls, String method, int line){}

    default void arrayAlloc(String cls, String method, int line, int dimensions, int capacity){}

    default void objectAlloc(String cls, String method, int line, String targetCls, String targetConstructorSignature){}

}
