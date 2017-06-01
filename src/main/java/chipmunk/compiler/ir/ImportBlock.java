package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ImportBlock extends Block {

	protected String module;
	protected List<String> symbols;
	protected List<String> aliases;
	
	public ImportBlock(){
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
	
	public void removeSymbol(String symbol){
		symbols.remove(symbol);
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
}
