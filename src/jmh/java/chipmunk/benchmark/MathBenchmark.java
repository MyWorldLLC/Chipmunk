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

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

import chipmunk.ChipmunkVM;
import chipmunk.compiler.ChipmunkLexer;
import chipmunk.compiler.ChipmunkParser;
import chipmunk.compiler.ast.AstNode;
import chipmunk.compiler.codegen.MethodVisitor;
import chipmunk.compiler.codegen.SymbolTableBuilderVisitor;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.truffle.ChipmunkLanguage;
import chipmunk.truffle.ast.*;

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
		
		@Setup(Level.Trial)
		public void initializeVM(){
			vm = new ChipmunkVM();
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionTruffle(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		Object[] constantPool = scripts.countToAMillion.getConstantPool();

		// value = vm.dispatch(countToAMillion, 0);
		FrameDescriptor desc = new FrameDescriptor();
		desc.addFrameSlot(0);

		FrameSlot local1 = desc.addFrameSlot(1);

		OpNode[] ops = new OpNode[15];
		ops[0] = new PushNode(vm, ops, 0, constantPool, 0);
		ops[1] = new SetLocalNode(vm, ops, 1, local1);
		ops[2] = new PopNode(vm, ops, 2);
		ops[3] = new GetLocalNode(vm, ops, 3, local1);
		ops[4] = new PushNode(vm, ops, 4, constantPool, 1);

		ops[5] = new LessThanNode(vm, ops, 5);
		ops[6] = new IfNode(vm, ops, 6, 13);
		ops[7] = new GetLocalNode(vm, ops, 7, local1);
		ops[8] = new PushNode(vm, ops, 8, constantPool, 2);
		ops[9] = new AddNode(vm, ops, 9);
		ops[10] = new SetLocalNode(vm, ops, 10, local1);
		ops[11] = new PopNode(vm, ops, 11);
		ops[12] = new GotoNode(vm, ops, 12, 3);

		ops[13] = new GetLocalNode(vm, ops, 13, local1);
		ops[14] = new ReturnNode(vm, ops, 14);

		for (int i = 0; i < ops.length; i++) {
			if (ops[i] instanceof StaticOpNode) {
				((StaticOpNode) ops[i]).link();
			}
		}

		MethodNode method = new MethodNode(null, desc);
		method.setOpNodes(ops);

		return method.getCallTarget().call();
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionCVM(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		CMethod method = scripts.countToAMillion;
		return vm.dispatch(method, method.getArgCount());
	}
	
//	@Benchmark
//	@BenchmarkMode(Mode.SampleTime)
//	public Object countToOneMillionJava(ChipmunkScripts scripts) {
//		int x = 0;
//		while(x < 1000000) {
//			x = x + 1;
//		}
//		return x;
//	}


	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(MathBenchmark.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();

	}

}
