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

package chipmunk.vm.jvm;

import chipmunk.binary.BinaryModule;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public class JvmCompilation {

    protected final BinaryModule module;
    protected String packagePrefix;

    protected final Deque<NamespaceInfo> namespaceInfo;

    public JvmCompilation(BinaryModule module){
        this.module = module;
        namespaceInfo = new ArrayDeque<>();
    }

    public BinaryModule getModule() {
        return module;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public String getPrefixedModuleName(){
        if(packagePrefix == null){
            return module.getName();
        }

        return packagePrefix + "." + module.getName();
    }

    public void enterNamespace(NamespaceInfo info){
        namespaceInfo.add(info);
    }

    public NamespaceInfo exitNamespace(){
        return namespaceInfo.pollLast();
    }

    public NamespaceInfo containingNamespace(){
        return namespaceInfo.peekLast();
    }

    public String qualifiedContainingName(){
        final String containingName = namespaceInfo.stream()
                .map(NamespaceInfo::getName)
                .collect(Collectors.joining("."));
        return packagePrefix != null ? packagePrefix + "." + containingName : containingName;
    }
}
