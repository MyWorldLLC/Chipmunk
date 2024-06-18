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
import chipmunk.vm.ModuleLoader;
import chipmunk.vm.invoke.security.LinkingPolicy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JvmCompilation {

    protected final BinaryModule module;
    protected final ModuleLoader loader;
    protected String packagePrefix;

    protected final Set<String> bindings;
    protected final Deque<NamespaceInfo> namespaceInfo;

    protected final JvmCompilerConfig config;

    public JvmCompilation(BinaryModule module, ModuleLoader loader, JvmCompilerConfig config){
        this.module = module;
        this.loader = loader;
        namespaceInfo = new ArrayDeque<>();
        bindings = new HashSet<>();

        this.config = config;
    }

    public BinaryModule getModule() {
        return module;
    }

    public ModuleLoader getLoader(){
        return loader;
    }

    public JvmCompilerConfig getConfig(){
        return config;
    }

    public LinkingPolicy getLinkingPolicy(){
        return config.getLinkingPolicy();
    }

    public TrapConfig getTrapConfig(){
        return config.getTrapConfig();
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

    public boolean isBindingDefined(String bindingSignature) {
        return bindings.contains(bindingSignature);
    }

    public void defineBinding(String bindingSignature){
        bindings.add(bindingSignature);
    }
}
