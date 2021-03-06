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

package chipmunk.modules.imports;

import chipmunk.runtime.ChipmunkModule;
import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;

public class ImportModule implements ChipmunkModule {

    public static final String IMPORT_MODULE_NAME = "chipmunk.imports";

    public ChipmunkModule importModule(String moduleName) throws Throwable {
        ChipmunkScript script = ChipmunkScript.getCurrentScript();
        ChipmunkVM vm = script.getVM();
        return vm.getModule(script, moduleName);
    }

    @Override
    public String getName(){
        return IMPORT_MODULE_NAME;
    }

}
