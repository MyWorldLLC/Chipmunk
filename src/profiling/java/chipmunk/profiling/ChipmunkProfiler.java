package chipmunk.profiling;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import chipmunk.ChipmunkVM;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.codegen.MethodVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;

public class ChipmunkProfiler {
	
	public static CMethod compileTestMethod(InputStream is) throws Exception {
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String last = reader.readLine();
		while(last != null){
			builder.append(last);
			last = reader.readLine();
		}
		
		ChipmunkLexer lexer = new ChipmunkLexer();
		ChipmunkParser parser = new ChipmunkParser(lexer.lex(builder.toString()));
		
		AstNode root = parser.parseMethodDef();
		root.visit(new SymbolTableBuilderVisitor());
		
		List<Object> constantPool = new ArrayList<Object>();
		MethodVisitor visitor = new MethodVisitor(constantPool, new CModule());
		
		root.visit(visitor);
		return visitor.getMethod();
	}
	
	public static void main(String[] args) throws Exception{
		
		CMethod countToAMillion = compileTestMethod(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"));
		ChipmunkVM vm = new ChipmunkVM();
		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.dispatch(countToAMillion, 0);
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value.toString() + ", Time: " + (endTime - startTime) / 10.0e9 + " seconds");
		}
		
	}

}
