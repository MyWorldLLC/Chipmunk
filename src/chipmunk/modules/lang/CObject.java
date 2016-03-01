package chipmunk.modules.lang;

import chipmunk.ChipmunkContext;
import chipmunk.Namespace;

public abstract class CObject {
	
	protected Namespace namespace;
	protected CType type;
	
	public CObject(){
		namespace = new Namespace();
	}
	
	public CType getType(){
		return type;
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
	
	public CObject __plus__(CObject other){
		throw new UnimplementedOperationException("Operation __plus__ not defined for type " + type.getName());
	}
	
	public CObject __minus__(CObject other){
		throw new UnimplementedOperationException("Operation __minus__ not defined for type " + type.getName());
	}
	
	public CObject __mul__(CObject other){
		throw new UnimplementedOperationException("Operation __mul__ not defined for type " + type.getName());
	}
	
	public CObject __div__(CObject other){
		throw new UnimplementedOperationException("Operation __div__ not defined for type " + type.getName());
	}
	
	public CObject __fdiv__(CObject other){
		throw new UnimplementedOperationException("Operation __fdiv__ not defined for type " + type.getName());
	}
	
	public CObject __rem__(CObject other){
		throw new UnimplementedOperationException("Operation __rem__ not defined for type " + type.getName());
	}
	
	public CObject __pow__(CObject other){
		throw new UnimplementedOperationException("Operation __pow__ not defined for type " + type.getName());
	}
	
	public CObject __inc__(){
		throw new UnimplementedOperationException("Operation __inc__ not defined for type " + type.getName());
	}
	
	public CObject __dec__(){
		throw new UnimplementedOperationException("Operation __dec__ not defined for type " + type.getName());
	}
	
	public CObject __pos__(){
		throw new UnimplementedOperationException("Operation __pos__ not defined for type " + type.getName());
	}
	
	public CObject __neg__(){
		throw new UnimplementedOperationException("Operation __neg__ not defined for type " + type.getName());
	}
	
	public CObject __and__(){
		throw new UnimplementedOperationException("Operation __and__ not defined for type " + type.getName());
	}
	
	public CObject __or__(){
		throw new UnimplementedOperationException("Operation __or__ not defined for type " + type.getName());
	}
	
	public CObject __xor__(){
		throw new UnimplementedOperationException("Operation __xor__ not defined for type " + type.getName());
	}
	
	public CObject __bneg__(){
		throw new UnimplementedOperationException("Operation __bneg__ not defined for type " + type.getName());
	}
	
	public CObject __lshift__(){
		throw new UnimplementedOperationException("Operation __lshift__ not defined for type " + type.getName());
	}
	
	public CObject __rshift__(){
		throw new UnimplementedOperationException("Operation __rshift__ not defined for type " + type.getName());
	}
	
	public CObject __urshift__(){
		throw new UnimplementedOperationException("Operation __urshift__ not defined for type " + type.getName());
	}
	
	public CObject __getAttr__(CObject name){
		throw new UnimplementedOperationException("Operation __getAttr__ not defined for type " + type.getName());
	}
	
	public void __setAttr__(CObject name, CObject value){
		throw new UnimplementedOperationException("Operation __setAttr__ not defined for type " + type.getName());
	}
	
	public CObject __getAt__(CObject index){
		throw new UnimplementedOperationException("Operation __getAt__ not defined for type " + type.getName());
	}
	
	public CObject __setAt__(CObject other){
		throw new UnimplementedOperationException("Operation __setAt__ not defined for type " + type.getName());
	}
	
	public CObject __call__(ChipmunkContext context, int paramCount, boolean resuming){
		throw new UnimplementedOperationException("Operation __call__ not defined for type " + type.getName());
	}
	
	public boolean __truth__(){
		throw new UnimplementedOperationException("Operation __truth__ not defined for type " + type.getName());
	}
}
