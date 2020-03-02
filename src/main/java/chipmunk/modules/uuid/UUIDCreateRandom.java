package chipmunk.modules.uuid;

import chipmunk.ChipmunkVM;
import chipmunk.modules.buffer.Buffer;
import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.runtime.*;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDCreateRandom implements CCallable {

    @Override
    public CObject call(ChipmunkVM vm, Object[] params) {
        vm.checkArity(params, 0);

        CClass uuidClass = (CClass) vm.getModule("chipmunk.uuid").getNamespace().get("UUID");
        BufferCClass cls = (BufferCClass) vm.getModule("chipmunk.buffer").getNamespace().get("Buffer");

        Buffer buf = cls.instantiate();
        buf.setData(new byte[16]);
        ByteBuffer bytes = ByteBuffer.wrap(buf.getData());

        UUID jUUID = UUID.randomUUID();
        bytes.putLong(jUUID.getMostSignificantBits());
        bytes.putLong(jUUID.getLeastSignificantBits());

        CObject cUUID = (CObject) uuidClass.instantiate();
        cUUID.setAttr(vm, "buffer", buf);

        vm.traceMem(16);
        vm.traceReference();
        return cUUID;
    }

}
