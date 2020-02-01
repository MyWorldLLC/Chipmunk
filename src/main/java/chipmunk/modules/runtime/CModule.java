package chipmunk.modules.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chipmunk.Namespace;

public class CModule {
	
	public static final String DEFAULT = "";
	
	public class Import {
		private final String name;
		private final boolean importAll;
		private final List<String> symbols;
		private final List<String> aliases;
		
		public Import(String name, boolean importAll){
			this.name = name;
			this.importAll = importAll;
			symbols = new ArrayList<String>();
			aliases = new ArrayList<String>();
		}
		
		public String getName(){
			return name;
		}
		
		public List<String> getSymbols(){
			return symbols;
		}
		
		public List<String> getAliases(){
			return aliases;
		}
		
		public boolean isImportAll(){
			return importAll;
		}
		
		public boolean isAliased(){
			return aliases.size() != 0;
		}
	}
	
	private final List<Object> constants;
	private final Namespace namespace;
	private final String name;
	private final List<Import> imports;
	
	private CMethod initializer;
	
	public CModule() {
		this(DEFAULT);
	}
	
	public CModule(String name){
		this(name, new ArrayList<>());
	}
	
	public CModule(String name, List<Object> constantPool){
		if(name == null){
			throw new NullPointerException("Module name cannot be null");
		}
		
		if(constantPool == null){
			throw new NullPointerException("Constant pool cannot be null");
		}
		
		this.name = name;
		constants = constantPool;
		namespace = new Namespace();
		imports = new ArrayList<>();
	}
	
	public List<Object> getConstantsUnmodifiable(){
		return Collections.unmodifiableList(constants);
	}
	
	public Namespace getNamespace(){
		return namespace;
	}
	
	public String getName(){
		return name;
	}
	
	public List<Import> getImports(){
		return imports;
	}
	
	public void setInitializer(CMethod initializer){
		this.initializer = initializer;
	}
	
	public CMethod getInitializer(){
		return initializer;
	}
	
	public boolean hasInitializer(){
		return initializer != null;
	}
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object other){
		if(other instanceof CModule){
			if((name.equals(((CModule) other).name))){
				return true;
			}
		}
		return false;
	}
}
