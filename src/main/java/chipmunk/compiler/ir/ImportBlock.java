package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ImportBlock {

	protected String module;
	protected List<String> symbols;
	
	public ImportBlock(){
		symbols = new ArrayList<String>();
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
}
