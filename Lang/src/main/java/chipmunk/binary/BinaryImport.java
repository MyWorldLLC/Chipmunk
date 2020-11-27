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

package chipmunk.binary;

public class BinaryImport {

    protected String name;
    protected boolean importAll;
    protected String[] symbols;
    protected String[] aliases;

    public BinaryImport(String name, boolean importAll){
        this.name = name;
        this.importAll = importAll;
    }

    public String getName(){
        return name;
    }

    public void setSymbols(String[] symbols){
        this.symbols = symbols;
    }

    public String[] getSymbols(){
        return symbols;
    }

    public void setAliases(String[] aliases){
        this.aliases = aliases;
    }

    public String[] getAliases(){
        return aliases;
    }

    public boolean isImportAll(){
        return importAll;
    }

    public boolean isAliased(){
        return aliases != null && aliases.length != 0;
    }
}
