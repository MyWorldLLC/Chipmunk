package chipmunk.modules.reflectiveruntime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chipmunk.ChipmunkVM;
import chipmunk.reflectors.VMOperator;

public class CList implements VMOperator {
	
	private final List<Object> list;
	
	public CList(){
		list = new ArrayList<Object>();
	}
	
	public CList(List<Object> list){
		this.list = list;
	}
	
	public void add(ChipmunkVM vm, Object obj){
		list.add(obj);
	}
	
	public void add(ChipmunkVM vm, Integer index, Object obj){
		list.add(index, obj);
	}
	
	public CBoolean addAll(ChipmunkVM vm, Collection<? extends Object> collection){
		vm.traceBoolean();
		return new CBoolean(list.addAll(collection));
	}
	
	public void clear(ChipmunkVM vm){
		list.clear();
	}
	
	public CBoolean contains(ChipmunkVM vm, Object obj){
		vm.traceBoolean();
		return new CBoolean(list.contains(obj));
	}
	
	public CBoolean containsAll(ChipmunkVM vm, Collection<? extends Object> collection){
		vm.traceBoolean();
		return new CBoolean(list.containsAll(collection));
	}
	
	public CBoolean equals(ChipmunkVM vm, Object obj){
		vm.traceBoolean();
		return new CBoolean(list.equals(obj));
	}
	
	public Object get(ChipmunkVM vm, Integer index){
		return list.get(index);
	}
	
	public CInteger hashCode(ChipmunkVM vm){
		vm.traceInteger();
		return new CInteger(list.hashCode());
	}
	
	public CInteger indexOf(ChipmunkVM vm, Object obj){
		vm.traceInteger();
		return new CInteger(list.indexOf(obj));
	}
	
	public CBoolean isEmpty(ChipmunkVM vm){
		vm.traceBoolean();
		return new CBoolean(list.isEmpty());
	}
	
	public CIterator iterator(ChipmunkVM vm){
		vm.traceMem(8 + 8 + 4); // size of iterator
		return new ListIterator(list);
	}
	
	public CInteger lastIndexOf(ChipmunkVM vm, Object obj){
		vm.traceInteger();
		return new CInteger(list.lastIndexOf(obj));
	}
	
	public CBoolean remove(ChipmunkVM vm, Integer index){
		vm.traceBoolean();
		return new CBoolean(list.remove(index));
	}
	
	public CBoolean removeAll(ChipmunkVM vm, Collection<? extends Object> collection){
		vm.traceBoolean();
		return new CBoolean(list.removeAll(collection));
	}
	
	public CBoolean retainAll(ChipmunkVM vm, Collection<? extends Object> collection){
		vm.traceBoolean();
		return new CBoolean(list.retainAll(collection));
	}
	
	public Object set(ChipmunkVM vm, Integer index, Object obj){
		return list.set(index, obj);
	}
	
	public CInteger size(ChipmunkVM vm){
		vm.traceInteger();
		return new CInteger(list.size());
	}
	
	public void sort(ChipmunkVM vm, CComparator comparator){
		list.sort(comparator);
	}
	
	public CList subList(ChipmunkVM vm, Integer from, Integer to){
		vm.traceMem(8); // size of CList reference
		return new CList(list.subList(from, to));
	}
	
	private class ListIterator implements CIterator {
		
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
}
