package chipmunk.modules.buffer;

import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CModule;

public class BufferCClass extends CClass {

    public BufferCClass(CModule module) {
        super("Buffer", module);
    }

    public Buffer call(ChipmunkVM vm, Object[] params){
        if(params.length != 0 || params.length != 1){
            throw new IllegalArgumentException(String.format("Buffer can only be instantiated with 0 or 1 parameters, not %d", params.length));
        }

        int size = 0;
        if(params.length == 1) {
            size = ((CInteger) params[0]).intValue();
        }

        vm.traceReference();
        vm.traceMem(size);
        return new Buffer(this, size);
    }
}
