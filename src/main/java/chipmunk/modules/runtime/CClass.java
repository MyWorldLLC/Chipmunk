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

import chipmunk.vm.ChipmunkVM;

public class CClass implements Initializable, CallInterceptor, CCallable {

	private final Namespace sharedAttributes;
	
	private final Namespace instanceAttributes;
	
	private final CString name;
	private final CModule module;
	
	private CMethod sharedInitializer;
	private CMethod instanceInitializer;
	
	public CClass(String name, CModule module){
		sharedAttributes = new Namespace();
		
		instanceAttributes = new Namespace();
		
		this.name = new CString(name);
		this.module = module;
	}
	
	public Namespace getAttributes(){
		return sharedAttributes;
	}
	
	public Namespace getInstanceAttributes(){
		return instanceAttributes;
	}

	public CString getName(ChipmunkVM vm){
		return name;
	}

	public CString getFullName(ChipmunkVM vm){
		//vm.traceReference();
		return new CString(getFullName());
	}

	public String getFullName(){
		return String.format("%s.%s", module.getName(), getName());
	}

	public String getName(){
		return name.toString();
	}
	
	public CModule getModule(){
		return module;
	}
	
	public Object call(ChipmunkVM vm, Object[] params){
		
		// TODO - memory tracing
		//vm.traceReference();
		CObject obj = (CObject) instantiate();
		
		// Invoke constructor (compiler ensures that all classes have exactly one constructor).
		// This is suspension/exception-safe because (a) any exceptions will seamlessly propagate
		// out of this method and back into the VM, and (b) any suspension will freeze the running
		// constructor, skip this, and continue freezing the previous call stack. Since this method
		// just returns what the constructor returns (the newly created object), when the call stack
		// is resumed this method will not be invoked, the constructor will resume where it left off,
		// and the VM will have pushed the newly created object onto the stack when the constructor
		// returns.
		//return vm.dispatch((CMethod)obj.getAttributes().get(name.toString()), params);
		return null;
	}

	public Object instantiate(){
		CObject obj = new CObject(this, instanceAttributes.duplicate());

		CMethod initializer = instanceInitializer.duplicate();
		initializer.bind(obj);

		obj.setInitializer(initializer);
		obj.getAttributes().set("class", this);

		return obj;
	}

	public CMethod getSharedInitializer() {
		return sharedInitializer;
	}

	public void setSharedInitializer(CMethod sharedInitializer) {
		this.sharedInitializer = sharedInitializer;
	}

	public CMethod getInstanceInitializer() {
		return instanceInitializer;
	}

	public void setInstanceInitializer(CMethod instanceInitializer) {
		this.instanceInitializer = instanceInitializer;
	}
	
	public Object setAttr(ChipmunkVM vm, String name, Object value){
		//vm.traceMem(8);
		sharedAttributes.set(name, value);
		return value;
	}

	public Object setAttr(ChipmunkVM vm, CString name, Object value){
		return setAttr(vm, name.toString(), value);
	}
	
	public Object getAttr(ChipmunkVM vm, String name){
		//vm.traceMem(8);
		return sharedAttributes.get(name);
	}

	public Object getAttr(ChipmunkVM vm, CString name){
		return getAttr(vm, name.toString());
	}
	
	public CBoolean instanceOf(ChipmunkVM vm, CClass clazz) {
		if(clazz.getName().equals("Class") && clazz.getModule().getName().equals("chipmunk.lang")) {
			//vm.traceBoolean();
			return new CBoolean(true);
		}
		//vm.traceBoolean();
		return new CBoolean(false);
	}

	@Override
	public CMethod getInitializer() {
		return sharedInitializer;
	}

	@Override
	public Object callAt(ChipmunkVM vm, String methodName, Object[] params) {
		CCallable callable = (CCallable) sharedAttributes.get(methodName);
		if(callable != null){
			return callable.call(vm, params);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "class " + module.getName() + "." + (!name.equals("") ? name : "<anonymous>");
	}
	
}
