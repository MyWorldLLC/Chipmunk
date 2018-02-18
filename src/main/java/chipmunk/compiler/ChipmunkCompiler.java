package chipmunk.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

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
	
	public List<CModule> compile(CharSequence src) throws CompileChipmunk, SyntaxErrorChipmunk {
		
		List<CModule> modules = new ArrayList<CModule>();
		ChipmunkLexer lexer = new ChipmunkLexer();
		TokenStream tokens = lexer.lex(src);
		
		ChipmunkParser parser = new ChipmunkParser(tokens);
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
		
		return modules;
	}
	
	public List<CModule> compile(InputStream src) throws IOException, CompileChipmunk, SyntaxErrorChipmunk {
		StringBuilder builder = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(src, Charset.forName("UTF-8")));
		String line = reader.readLine();
		while(line != null){
			builder.append(line);
			line = reader.readLine();
		}
		
		return compile(builder);
	}

}
