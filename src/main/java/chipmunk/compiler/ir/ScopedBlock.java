package chipmunk.compiler.ir;

public class ScopedBlock extends Block {
	
	protected Scope scope;
	
	public ScopedBlock(Scope outer){
		scope = new Scope();
		scope.setEnclosing(outer);
	}
	
	public ScopedBlock(){
		scope = new Scope();
	}
	
	public Scope getScope(){
		return scope;
	}
}