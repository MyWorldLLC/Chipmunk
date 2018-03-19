package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import chipmunk.ChipmunkScript;
import chipmunk.compiler.ast.AstVisitor;
import chipmunk.compiler.ast.ModuleNode;
import chipmunk.compiler.codegen.ModuleVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.reflectiveruntime.CModule;

public class ChipmunkCompiler {
	
	protected List<AstVisitor> visitors;
	
	public ChipmunkCompiler(){
		visitors = new ArrayList<AstVisitor>();
		visitors.add(new SymbolTableBuilderVisitor());
	}
	
	public List<AstVisitor> getVisitors(){
		return visitors;
	}
	
	public ChipmunkScript compile(CharSequence src, String fileName) throws CompileChipmunk, SyntaxErrorChipmunk {
		
		List<CModule> modules = new ArrayList<CModule>();
		ChipmunkLexer lexer = new ChipmunkLexer();
		TokenStream tokens = lexer.lex(src);
		
		ChipmunkParser parser = new ChipmunkParser(tokens);
		parser.setFileName(fileName);
		parser.parse();
		List<ModuleNode> roots = parser.getModuleRoots();
		
		for(ModuleNode node : roots){
			
			for(AstVisitor visitor : visitors){
				node.visit(visitor);
			}
			
			ModuleVisitor visitor = new ModuleVisitor();
			node.visit(visitor);
			modules.add(visitor.getModule());
		}
		
		ChipmunkScript script = new ChipmunkScript();
		
		for(CModule module : modules){
			script.getModules().put(module.getName(), module);
		}
		
		return script;
	}
	
	public ChipmunkScript compile(InputStream src, String fileName) throws IOException, CompileChipmunk, SyntaxErrorChipmunk {
		StringBuilder builder = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(src, Charset.forName("UTF-8")));
		int character = reader.read();
		while(character != -1){
			builder.append((char) character);
			character = reader.read();
		}
		
		return compile(builder, fileName);
	}

}
