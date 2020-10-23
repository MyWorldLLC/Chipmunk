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
import chipmunk.modules.runtime.CCallable;
import chipmunk.modules.runtime.CObject;
import chipmunk.modules.runtime.CString;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDToString implements CCallable {
    @Override
    public Object call(ChipmunkVM vm, Object[] params) {
        //vm.checkArity(params, 1);

        /*CObject uuid = (CObject) params[0];
        Buffer buf = (Buffer) uuid.getAttr(vm, "buffer");

        ByteBuffer bytes = ByteBuffer.wrap(buf.getData());
        UUID jUUID = new UUID(bytes.getLong(), bytes.getLong());

        String str = jUUID.toString();
        vm.traceString(str);
        return new CString(str);*/
        return null;
    }
}
