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

package chipmunk.invoke;

import chipmunk.invoke.CallSignature;

import java.util.HashMap;
import java.util.Map;

public class CallCache {

    protected final Map<CallSignature, Object> cache;

    public CallCache(){
        cache = new HashMap<>();
    }

    public Object getTarget(CallSignature signature){
        return cache.get(signature);
    }

    public void cacheTarget(CallSignature signature, Object target){
        cache.put(signature, target);
    }

}
