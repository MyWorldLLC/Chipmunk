package chipmunk.compiler;

public class Symbol {
	
	protected boolean isShared;
	protected boolean isFinal;
	protected boolean isClosure;
	protected String name;
	
	public Symbol(){
		this("", false, false, false);
	}
	
	public Symbol(String name){
		this(name, false, false, false);
	}
	
	public Symbol(String name, boolean isFinal){
		this(name, isFinal, false, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared) {
		this(name, isFinal, isShared, false);
	}
	
	public Symbol(String name, boolean isFinal, boolean isShared, boolean isClosure){
		this.name = name;
		this.isFinal = isFinal;
		this.isShared = isShared;
		this.isClosure = isClosure;
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
	
	public boolean isClosure() {
		return isClosure;
	}
	
	public void setClosure(boolean isClosure) {
		this.isClosure = isClosure;
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
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		if(isShared){
			builder.append("shared ");
		}
		
		if(isFinal){
			builder.append("final ");
		}
		
		builder.append(name);
		
		return builder.toString();
	}
	
}
