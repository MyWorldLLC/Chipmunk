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

package chipmunk.modules.runtime;

import java.util.List;

import chipmunk.ChipmunkVM;
import chipmunk.Namespace;
import chipmunk.RuntimeObject;

public class CObject implements RuntimeObject, Initializable, CallInterceptor {
	
	private final CClass cClass;
	
	private final Namespace attributes;
	
	protected CMethod initializer;
	
	public CObject(CClass cls, Namespace attributes){
		cClass = cls;
		
		this.attributes = attributes;
		
		for(String name : attributes.names()){
			Object attr = attributes.get(name);
			
			if(attr instanceof CMethod){
				((CMethod) attr).bind(this);
			}
		}
	}
	
	public CClass getCClass(){
		return cClass;
	}
	
	public CClass getClass(ChipmunkVM vm){
		return cClass;
	}
	
	public Namespace getAttributes(){
		return attributes;
	}

	public CMethod getInitializer(){
		return initializer;
	}
	
	public boolean hasInitializer(){
		return initializer != null;
	}
	
	public void setInitializer(CMethod initializer){
		this.initializer = initializer;
	}
	
	public Object setAttr(ChipmunkVM vm, String name, Object value){
		vm.traceReference();
		attributes.set(name, value);
		return value;
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		return attributes.get(name);
	}
	
	public CBoolean instanceOf(ChipmunkVM vm, CClass clazz) {
		if(clazz == cClass) {
			vm.traceBoolean();
			return new CBoolean(true);
		}else {
			List<Object> traitAttributes = attributes.traitAttributes();
			for(int i = 0; i < traitAttributes.size(); i++) {
				Object trait = traitAttributes.get(i);
				if(trait instanceof CObject) {
					CBoolean isInstance = ((CObject) trait).instanceOf(vm, clazz);
					if(isInstance.booleanValue()) {
						vm.traceBoolean();
						return isInstance;
					}
				}else if(trait instanceof CClass) {
					CBoolean isInstance = ((CClass) trait).instanceOf(vm, clazz);
					if(isInstance.booleanValue()) {
						vm.traceBoolean();
						return isInstance;
					}
				}
			}
		}
		vm.traceBoolean();
		return new CBoolean(false);
	}

	public CBoolean equals(ChipmunkVM vm, Object other){
		vm.traceBoolean();
		return new CBoolean(other == this);
	}
	
	@Override
	public Object callAt(ChipmunkVM vm, String methodName, Object[] params) {
		CCallable callable = (CCallable) attributes.get(methodName);
		if(callable != null){
			return callable.call(vm, params);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return cClass.getName() + '@' + Integer.toHexString(System.identityHashCode(this));
	}

}
