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

package chipmunk.vm;

public class EntryPoint {

    protected String module;
    protected String method;

    public EntryPoint(){
        this("main", "main");
    }

    public EntryPoint(String module){
        this(module, "main");
    }

    public EntryPoint(String module, String method){

        if(module == null || module.isEmpty()){
            throw new IllegalArgumentException("module must be specified");
        }

        if(method == null || method.isEmpty()){
            throw new IllegalArgumentException("method must be specified");
        }

        this.module = module;
        this.method = method;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(module);
        sb.append("::");
        sb.append(method);

        return sb.toString();
    }

    public static EntryPoint fromString(String entrypoint) throws IllegalArgumentException {

        entrypoint = entrypoint.trim();
        if(entrypoint.isEmpty()){
            throw new IllegalArgumentException("entrypoint is empty");
        }

        int splitIndex = entrypoint.indexOf("::");

        if(splitIndex == 0){
            throw new IllegalArgumentException("module is empty");
        }

        if(splitIndex + 2 >= entrypoint.length() - 1){
            throw new IllegalArgumentException("method is empty");
        }

        if(splitIndex == -1){
            // No method is specified
            return new EntryPoint(entrypoint, "main");
        }

        String module = entrypoint.substring(0, splitIndex);
        String method = entrypoint.substring(splitIndex + 2);

        return new EntryPoint(module, method);
    }
}
