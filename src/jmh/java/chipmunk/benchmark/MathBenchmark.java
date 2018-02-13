package chipmunk.benchmark;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import chipmunk.ChipmunkVM;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.codegen.MethodVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.reflectiveruntime.CMethod;

public class MathBenchmark {
	
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
		MethodVisitor visitor = new MethodVisitor(constantPool);
		
		root.visit(visitor);
		return visitor.getMethod();
	}
	
	@State(Scope.Thread)
	public static class ChipmunkScripts {
		
		public CMethod countToAMillion;
		
		public ChipmunkVM vm;
		
		@Setup(Level.Trial)
		public void compileSources() throws Exception {
			countToAMillion = compileTestMethod(MathBenchmark.class.getResourceAsStream("CountToAMillion.chp"));
		}
		
		@Setup(Level.Invocation)
		public void initializeVM(){
			vm = new ChipmunkVM();
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillion(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		CMethod method = scripts.countToAMillion;
		return vm.dispatch(method, method.getArgCount());
	}

	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(MathBenchmark.class.getSimpleName())
				.forks(1)
				.addProfiler(StackProfiler.class)
				.build();

		new Runner(opt).run();

	}

}
