package chipmunk.compiler.ir;

import java.util.ArrayList;
import java.util.List;

public class ModuleBlock extends Block {
	
	protected String name;
	protected List<ImportBlock> imports;
	protected List<VarDecBlock> variableDeclarations;
	protected List<MethodBlock> methodDeclarations;
	protected List<ClassBlock> classDeclarations;
	
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
	
	public List<VarDecBlock> getVariableDeclarations(){
		return variableDeclarations;
	}

	public void addVariableDeclaration(VarDecBlock variableDeclaration){
		variableDeclarations.add(variableDeclaration);
	}

	public List<MethodBlock> getMethodDeclarations(){
		return methodDeclarations;
	}

	public void addMethodDeclaration(MethodBlock methodDeclaration){
		methodDeclarations.add(methodDeclaration);
	}

	public List<ClassBlock> getClassDeclarations(){
		return classDeclarations;
	}

	public void addClassDeclaration(ClassBlock classDeclaration){
		classDeclarations.add(classDeclaration);
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
}
