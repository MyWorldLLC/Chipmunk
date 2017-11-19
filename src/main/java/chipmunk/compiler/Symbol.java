package chipmunk.compiler;

public class Symbol {
	
	protected boolean isShared;
	protected boolean isFinal;
	protected String name;
	
	public Symbol(){
		this("", false, false);
	}
	
	public Symbol(String name){
		this(name, false, false);
	}
	
	public Symbol(String name, boolean isFinal){
		this(name, isFinal, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared){
		this.name = name;
		this.isFinal = isFinal;
		this.isShared = isShared;
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
	
	@Override
	public boolean equals(Object other){
		if(other instanceof Symbol){
			Symbol otherSymbol = (Symbol) other;
			return otherSymbol.getName().equals(name);
		}
		return false;
	}
	
}
