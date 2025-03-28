/*
 * Copyright (C) 2025 MyWorld, LLC
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

package chipmunk.runtime;

import chipmunk.vm.invoke.security.AllowChipmunkLinkage;

public class CModule extends CObject implements ChipmunkModule {

    protected final String name;
    protected String fileName;

    public CModule(String name) {
        this.name = name;
    }

    @Override
    @AllowChipmunkLinkage
    public String getName(){
        return name;
    }

    public void setFileName(String name){
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }
}
