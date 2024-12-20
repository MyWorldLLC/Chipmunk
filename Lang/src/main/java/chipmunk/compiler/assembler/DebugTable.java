/*
 * Copyright (C) 2024 MyWorld, LLC
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

package chipmunk.compiler.assembler;

import chipmunk.binary.DebugEntry;

import java.util.ArrayList;
import java.util.List;

public class DebugTable {

    private final List<DebugEntry> debugTable = new ArrayList<>();

    public void onLine(int lineNumber, int ip) {
        if(debugTable.isEmpty()) {

            DebugEntry debug = new DebugEntry();
            debug.beginIndex = ip;
            debug.lineNumber = lineNumber;

            debugTable.add(debug);

            return;
        }else {

            DebugEntry dbg = debugTable.get(debugTable.size() - 1);

            if(dbg.lineNumber != lineNumber) {
                dbg.endIndex = ip;

                DebugEntry next = new DebugEntry();
                next.beginIndex = ip;
                next.lineNumber = lineNumber;

                debugTable.add(next);
            }
        }

    }

    public void closeLine(int ip) {
        if(!debugTable.isEmpty()) {
            debugTable.getLast().endIndex = ip;
        }
    }

    public List<DebugEntry> getEntries(){
        return debugTable;
    }

}
