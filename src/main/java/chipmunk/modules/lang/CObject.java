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

package chipmunk.modules.lang;

import chipmunk.AngryChipmunk;
import chipmunk.ChipmunkVM;
import chipmunk.Namespace;
import chipmunk.nut.InputCapsule;
import chipmunk.nut.NutCracker;
import chipmunk.nut.NutPacker;
import chipmunk.nut.OutputCapsule;

public abstract class CObject {
	
	protected CType type;
	protected Namespace namespace;
	
	public CObject(){
		namespace = new Namespace();
	}
	
	public CType getType(){
		return type;
	}
	
	public CObject __plus__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __plus__ not defined for type " + type.getName());
	}
	
	public CObject __minus__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __minus__ not defined for type " + type.getName());
	}
	
	public CObject __mul__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __mul__ not defined for type " + type.getName());
	}
	
	public CObject __div__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __div__ not defined for type " + type.getName());
	}
	
	public CObject __fdiv__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __fdiv__ not defined for type " + type.getName());
	}
	
	public CObject __rem__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __rem__ not defined for type " + type.getName());
	}
	
	public CObject __pow__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __pow__ not defined for type " + type.getName());
	}
	
	public CObject __inc__(){
		throw new UnimplementedOperationChipmunk("Operation __inc__ not defined for type " + type.getName());
	}
	
	public CObject __dec__(){
		throw new UnimplementedOperationChipmunk("Operation __dec__ not defined for type " + type.getName());
	}
	
	public CObject __pos__(){
		throw new UnimplementedOperationChipmunk("Operation __pos__ not defined for type " + type.getName());
	}
	
	public CObject __neg__(){
		throw new UnimplementedOperationChipmunk("Operation __neg__ not defined for type " + type.getName());
	}
	
	public CObject __and__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __and__ not defined for type " + type.getName());
	}
	
	public CObject __or__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __or__ not defined for type " + type.getName());
	}
	
	public CObject __bxor__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __xor__ not defined for type " + type.getName());
	}
	
	public CObject __band__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __xor__ not defined for type " + type.getName());
	}
	
	public CObject __bor__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __xor__ not defined for type " + type.getName());
	}
	
	public CObject __bneg__(){
		throw new UnimplementedOperationChipmunk("Operation __bneg__ not defined for type " + type.getName());
	}
	
	public CObject __lshift__(CObject places){
		throw new UnimplementedOperationChipmunk("Operation __lshift__ not defined for type " + type.getName());
	}
	
	public CObject __rshift__(CObject places){
		throw new UnimplementedOperationChipmunk("Operation __rshift__ not defined for type " + type.getName());
	}
	
	public CObject __urshift__(CObject places){
		throw new UnimplementedOperationChipmunk("Operation __urshift__ not defined for type " + type.getName());
	}
	
	public CObject __getAttr__(CObject name){
		throw new UnimplementedOperationChipmunk("Operation __getAttr__ not defined for type " + type.getName());
	}
	
	public void __setAttr__(CObject name, CObject value){
		throw new UnimplementedOperationChipmunk("Operation __setAttr__ not defined for type " + type.getName());
	}
	
	public CObject __getAt__(CObject index){
		throw new UnimplementedOperationChipmunk("Operation __getAt__ not defined for type " + type.getName());
	}
	
	public CObject __setAt__(CObject index, CObject value){
		throw new UnimplementedOperationChipmunk("Operation __setAt__ not defined for type " + type.getName());
	}
	
	public CObject __call__(ChipmunkVM context, int paramCount, boolean resuming) throws AngryChipmunk {
		throw new UnimplementedOperationChipmunk("Operation __call__ not defined for type " + type.getName());
	}
	
	public boolean __truth__(){
		throw new UnimplementedOperationChipmunk("Operation __truth__ not defined for type " + type.getName());
	}
	
	public CObject __as__(CObject convertType){
		throw new UnimplementedOperationChipmunk("Operation __as__ not defined for type " + type.getName());
	}
	
	public int __compare__(CObject other){
		throw new UnimplementedOperationChipmunk("Operation __compare__ not defined for type " + type.getName());
	}
	
	public void __prePack__(NutPacker packer){}
	
	public void __pack__(NutPacker packer, OutputCapsule out){
		throw new UnimplementedOperationChipmunk("Operation __pack__ not defined for type " + type.getName());
	}
	
	public void __unpack__(NutCracker cracker, InputCapsule in){
		throw new UnimplementedOperationChipmunk("Operation __unpack__ not defined for type " + type.getName());
	}
	
	public int __hash__(){
		return this.hashCode();
	}
	
	public CString __string__(){
		return new CString(this.toString());
	}
}
