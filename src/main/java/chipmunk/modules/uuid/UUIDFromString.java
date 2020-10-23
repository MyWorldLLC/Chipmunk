/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.modules.uuid;

import chipmunk.ChipmunkVM;
import chipmunk.modules.buffer.Buffer;
import chipmunk.modules.buffer.BufferCClass;
import chipmunk.modules.runtime.CCallable;
import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CObject;
import chipmunk.modules.runtime.CString;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDFromString implements CCallable {
    @Override
    public Object call(ChipmunkVM vm, Object[] params) {
        //vm.checkArity(params, 1);

       /* CClass uuidClass = (CClass) vm.getModule("chipmunk.uuid").getNamespace().get("UUID");
        BufferCClass cls = (BufferCClass) vm.getModule("chipmunk.buffer").getNamespace().get("Buffer");

        Buffer buf = cls.instantiate();
        buf.setData(new byte[16]);
        ByteBuffer bytes = ByteBuffer.wrap(buf.getData());

        UUID jUUID = UUID.fromString(((CString) params[0]).stringValue());
        bytes.putLong(jUUID.getMostSignificantBits());
        bytes.putLong(jUUID.getLeastSignificantBits());

        CObject cUUID = (CObject) uuidClass.instantiate();
        cUUID.setAttr(vm, "buffer", buf);

        vm.traceMem(16);
        vm.traceReference();
        return cUUID;*/
        return null;
    }
}
