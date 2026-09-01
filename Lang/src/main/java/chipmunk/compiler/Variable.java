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

package chipmunk.compiler;

import chipmunk.compiler.ir.ParentNode;
import chipmunk.compiler.types.BuiltinTypes;
import chipmunk.compiler.types.ObjectType;

public class Variable extends Named {

    public static final int UPVALUE =    0b1;
    public static final int FINAL   =   0b10;
    public static final int TRAIT   =  0b100;
    public static final int SHARED  = 0b1000;

    protected final ParentNode declarationSite;
    protected final Import imp;
    protected int flags;
    protected ObjectType type;
    protected ObjectType declaredType;

    public Variable(String name, ParentNode declarationSite) {
        this(name, declarationSite, null);
    }

    public Variable(String name, ParentNode declarationSite, Import imp) {
        super(name);
        this.declarationSite = declarationSite;
        this.imp = imp;
    }

    public ParentNode declarationSite() {
        return declarationSite;
    }

    public int flags() {
        return flags;
    }

    public void flags(int flags) {
        this.flags = flags;
    }

    public void setFlag(int flag){
        this.flags |= flag;
    }

    public boolean isFlagSet(int flag){
        return (flags & flag) != 0;
    }

    public boolean isFinal(){
        return isFlagSet(FINAL);
    }

    public boolean isTrait(){
        return isFlagSet(TRAIT);
    }

    public boolean isUpValue(){
        return isFlagSet(UPVALUE);
    }

    public boolean isShared(){
        return isFlagSet(SHARED);
    }

    public ObjectType type() {
        return type;
    }

    public void type(ObjectType type) {
        this.type = type;
    }

    public ObjectType declaredType() {
        return declaredType;
    }

    public void declaredType(ObjectType declaredType) {
        this.declaredType = declaredType;
    }

    public boolean isImported(){
        return imp != null;
    }

    public Import imp() {
        return imp;
    }
}
