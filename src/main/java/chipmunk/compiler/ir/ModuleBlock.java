package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ModuleBlock extends ParentBlock {
	
	protected String name;
	protected List<ImportBlock> imports;
	
	public ModuleBlock(){
		super();
		imports = new ArrayList<ImportBlock>();
		name = "";
	}
	
	public void addImport(ImportBlock irImport){
		imports.add(irImport);
	}
	
	public void removeImport(ImportBlock irImport){
		imports.remove(irImport);
	}
	
	public List<ImportBlock> getImports(){
		return imports;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
