package chipmunk.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class ImportNode extends AstNode {

	protected String module;
	protected List<String> symbols;
	protected List<String> aliases;
	
	public ImportNode(){
		super();
		symbols = new ArrayList<String>();
		aliases = new ArrayList<String>();
	}
	
	public String getModule(){
		return module;
	}
	
	public void setModule(String modName){
		module = modName;
	}
	
	public void addSymbol(String symbol){
		symbols.add(symbol);
	}
	
	public void addAlias(String alias){
		aliases.add(alias);
	}
	
	public void removeSymbol(String symbol){
		symbols.remove(symbol);
	}
	
	public void removeAlias(String alias){
		aliases.remove(alias);
	}
	
	public boolean hasSymbol(String symbol){
		return symbols.contains(symbol);
	}
	
	public List<String> getSymbols(){
		return symbols;
	}
	
	public List<String> getAliases(){
		return aliases;
	}
	
	public boolean hasAliases(){
		return aliases.size() != 0;
	}
}
