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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

import chipmunk.ChipmunkVM;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.codegen.MethodVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CModule;
import chipmunk.truffle.ast.BlockNode;
import chipmunk.truffle.ast.MethodNode;
import chipmunk.truffle.ast.ReadLocalNode;
import chipmunk.truffle.ast.ReadLocalNodeGen;
import chipmunk.truffle.ast.StatementNode;
import chipmunk.truffle.ast.WriteLocalNode;
import chipmunk.truffle.ast.WriteLocalNodeGen;
import chipmunk.truffle.ast.flow.WhileNode;
import chipmunk.truffle.ast.literal.IntegerLiteralNode;
import chipmunk.truffle.ast.operators.AddNode;
import chipmunk.truffle.ast.operators.AddNodeGen;
import chipmunk.truffle.ast.operators.LessThanNode;
import chipmunk.truffle.ast.operators.LessThanNodeGen;

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
		MethodVisitor visitor = new MethodVisitor(constantPool, new CModule());
		
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
		
		public MethodNode truffleMethod;
		
		@Setup(Level.Trial)
		public void makeTruffleMethod() {
			FrameSlot slot = new FrameDescriptor().addFrameSlot(0);
			String varName = "x";
			
			WriteLocalNode writeX = WriteLocalNodeGen.create(new IntegerLiteralNode(0), slot.getIndex());
			ReadLocalNode readX = ReadLocalNodeGen.create(varName);
			IntegerLiteralNode oneMillion = new IntegerLiteralNode(1000000);
			LessThanNode condition = LessThanNodeGen.create(readX, oneMillion);
			
			ReadLocalNode readXInBody = ReadLocalNodeGen.create(varName);
			IntegerLiteralNode one = new IntegerLiteralNode(1);
			AddNode add = AddNodeGen.create(readXInBody, one);
			WriteLocalNode writeXInBody = WriteLocalNodeGen.create(add, slot.getIndex());
			BlockNode whileBody = new BlockNode(new StatementNode[]{writeXInBody});
			
			WhileNode whileLoop = new WhileNode(condition, whileBody);
			
			BlockNode methodBody = new BlockNode(new StatementNode[] {writeX, whileLoop});
			
			truffleMethod = new MethodNode(methodBody);
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionCVM(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		CMethod method = scripts.countToAMillion;
		return vm.dispatch(method, method.getArgCount());
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionTruffle(ChipmunkScripts scripts) {
		return scripts.truffleMethod.getCallTarget().call();
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionJava(ChipmunkScripts scripts) {
		int x = 0;
		while(x < 1000000) {
			x = x + 1;
		}
		return x;
	}


	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(MathBenchmark.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();

	}

}
