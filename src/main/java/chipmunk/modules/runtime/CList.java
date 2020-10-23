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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import chipmunk.ChipmunkVM;

public class CList {
	
	private final List<Object> list;
	
	public CList(){
		list = new ArrayList<Object>();
	}
	
	public CList(List<Object> list){
		this.list = list;
	}

	public List<Object> getBackingList(){
		return list;
	}

	public void add(Object obj){ list.add(obj); }
	
	public void add(ChipmunkVM vm, Object obj){
		list.add(obj);
	}
	
	public void add(ChipmunkVM vm, CInteger index, Object obj){
		list.add(index.intValue(), obj);
	}
	
	public CBoolean addAll(ChipmunkVM vm, Collection<? extends Object> collection){
		//vm.traceBoolean();
		return new CBoolean(list.addAll(collection));
	}
	
	public void clear(ChipmunkVM vm){
		list.clear();
	}
	
	public CBoolean contains(ChipmunkVM vm, Object obj){
		//vm.traceBoolean();
		return new CBoolean(list.contains(obj));
	}
	
	public CBoolean containsAll(ChipmunkVM vm, Collection<? extends Object> collection){
		//vm.traceBoolean();
		return new CBoolean(list.containsAll(collection));
	}
	
	public CBoolean equals(ChipmunkVM vm, Object obj){
		//vm.traceBoolean();
		return new CBoolean(list.equals(obj));
	}
	
	public Object get(ChipmunkVM vm, CInteger index){
		return list.get(index.intValue());
	}
	
	public Object get(CInteger index){
		return list.get(index.intValue());
	}
	
	public Object get(Integer index){
		return list.get(index);
	}
	
	public Object getAt(ChipmunkVM vm, CInteger index){
		return list.get(index.getValue());
	}
	
	public Object setAt(ChipmunkVM vm, CInteger index, Object value){
		return list.set(index.intValue(), value);
	}
	
	public CInteger hashCode(ChipmunkVM vm){
		//vm.traceInteger();
		return new CInteger(list.hashCode());
	}
	
	public CInteger indexOf(ChipmunkVM vm, Object obj){
		//vm.traceInteger();
		return new CInteger(list.indexOf(obj));
	}
	
	public CBoolean isEmpty(ChipmunkVM vm){
		//vm.traceBoolean();
		return new CBoolean(list.isEmpty());
	}
	
	public CIterator iterator(ChipmunkVM vm){
		//vm.traceMem(8 + 8 + 4); // size of iterator
		return new ListIterator(list);
	}
	
	public CInteger lastIndexOf(ChipmunkVM vm, Object obj){
		//vm.traceInteger();
		return new CInteger(list.lastIndexOf(obj));
	}
	
	public Object remove(CInteger index){
		return list.remove(index.intValue());
	}
	
	public CBoolean remove(ChipmunkVM vm, Object o){
		//vm.traceBoolean();
		return new CBoolean(list.remove(o));
	}
	
	public CBoolean removeAll(ChipmunkVM vm, Collection<? extends Object> collection){
		//vm.traceBoolean();
		return new CBoolean(list.removeAll(collection));
	}
	
	public CBoolean retainAll(ChipmunkVM vm, Collection<? extends Object> collection){
		//vm.traceBoolean();
		return new CBoolean(list.retainAll(collection));
	}
	
	public Object set(ChipmunkVM vm, Integer index, Object obj){
		return list.set(index, obj);
	}
	
	public CInteger size(ChipmunkVM vm){
		//vm.traceInteger();
		return new CInteger(list.size());
	}
	
	public int size(){
		return list.size();
	}
	
	public void sort(ChipmunkVM vm, CComparator comparator){
		list.sort(comparator);
	}
	
	public void reverse(){
		Collections.reverse(list);
	}
	
	public void reverse(ChipmunkVM vm){
		Collections.reverse(list);
	}
	
	public CList subList(ChipmunkVM vm, Integer from, Integer to){
		//vm.traceMem(8); // size of CList reference
		return new CList(list.subList(from, to));
	}
	
	protected class ListIterator implements CIterator {
		
		private final List<Object> list;
		private int index;

		public ListIterator(List<Object> list){
			this.list = list;
			index = 0;
		}
		@Override
		public Object next(ChipmunkVM vm) {
			if(hasNext(vm)){
				Object obj = list.get(index);
				index++;
				return obj;
			}
			throw new IllegalStateException();
		}

		@Override
		public boolean hasNext(ChipmunkVM vm) {
			return index < list.size();
		}
		
	}
	
	@Override
	public String toString(){
		return list.toString();
	}

	public CString toString(ChipmunkVM vm){
		String str = list.toString();
		//vm.traceString(str);
		return new CString(str);
	}
}
