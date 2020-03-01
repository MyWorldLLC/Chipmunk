package chipmunk.modules.buffer;

import chipmunk.ChipmunkVM;
import chipmunk.RuntimeObject;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CNull;

import java.util.Arrays;

public class Buffer implements RuntimeObject {

    protected final BufferCClass cClass;
    private byte[] data;

    public Buffer(BufferCClass cls, int initialSize){
        cClass = cls;
        data = new byte[initialSize];
    }

    public CInteger getAt(ChipmunkVM vm, CInteger index){
        return new CInteger(data[index.intValue()]);
    }

    public CInteger setAt(ChipmunkVM vm, CInteger index, CInteger value){
        data[index.intValue()] = (byte) value.intValue();
        return value;
    }

    public CInteger size(ChipmunkVM vm){
        return new CInteger(data.length);
    }

    public CNull resize(ChipmunkVM vm, CInteger newSize){

        final int size = newSize.intValue();
        vm.traceMem(size - data.length); // This will correctly trace size reductions too

        data = Arrays.copyOf(data, size);

        return CNull.instance();
    }

    public CClass getClass(ChipmunkVM vm){
        return cClass;
    }
}
