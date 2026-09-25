/*
 * Copyright (C) 2026 MyWorld, LLC
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

public class CImport {

    protected final String name;
    protected final String[] symbols;
    protected final String[] aliases;

    public CImport(String name){
        this(name, new String[]{}, new String[]{});
    }

    public CImport(String name, String[] symbols){
        this(name, symbols, new String[]{});
    }

    public CImport(String name, String[] symbols, String[] aliases) {
        this.name = name;
        if(aliases.length > 0 && symbols.length != aliases.length){
            throw new IllegalArgumentException("Mismatched symbol and alias imports: %d symbols, %d aliases".formatted(symbols.length, aliases.length));
        }
        this.symbols = symbols;
        this.aliases = aliases;
    }

    public String name(){
        return name;
    }

    public String[] symbols(){
        return symbols;
    }

    public String[] aliases(){
        return aliases;
    }

    public boolean isAliased(){
        return aliases.length > 0;
    }

    public boolean isImportAll(){
        return symbols.length == 0;
    }
}
