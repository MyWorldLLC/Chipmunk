package chipmunk.modules.uuid;

import chipmunk.ChipmunkVM;
import chipmunk.modules.buffer.Buffer;
import chipmunk.modules.runtime.CCallable;
import chipmunk.modules.runtime.CObject;
import chipmunk.modules.runtime.CString;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDToString implements CCallable {
    @Override
    public Object call(ChipmunkVM vm, Object[] params) {
        vm.checkArity(params, 1);

        CObject uuid = (CObject) params[0];
        Buffer buf = (Buffer) uuid.getAttr(vm, "buffer");

        ByteBuffer bytes = ByteBuffer.wrap(buf.getData());
        UUID jUUID = new UUID(bytes.getLong(), bytes.getLong());

        String str = jUUID.toString();
        vm.traceString(str);
        return new CString(str);
    }
}
