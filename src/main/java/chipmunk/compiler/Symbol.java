package chipmunk.compiler;


public class Symbol {
	
	protected boolean isShared;
	protected boolean isFinal;
	protected String name;
	protected int localIndex;
	protected SymbolTable table;
	
	public Symbol(){
		this("", -1);
	}
	
	public Symbol(String name){
		this(name, -1);
	}
	
	public Symbol(String name, int localIndex){
		isShared = false;
		isFinal = false;
		this.name = name;
		localIndex = localIndex;
	}

	public boolean isShared(){
		return isShared;
	}

	public void setShared(boolean isShared){
		this.isShared = isShared;
	}

	public boolean isFinal(){
		return isFinal;
	}

	public void setFinal(boolean isFinal){
		this.isFinal = isFinal;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public int getLocalIndex(){
		return localIndex;
	}

	public void setLocalIndex(int localIndex){
		this.localIndex = localIndex;
	}

	public SymbolTable getTable(){
		return table;
	}

	public void setTable(SymbolTable table){
		this.table = table;
		
	}
	
}
