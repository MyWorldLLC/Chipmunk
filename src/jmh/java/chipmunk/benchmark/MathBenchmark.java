package chipmunk.benchmark;

import java.io.InputStream;

import chipmunk.modules.runtime.CMethod;
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

import chipmunk.ChipmunkScript;
import chipmunk.ChipmunkVM;

public class MathBenchmark {
	
	public static ChipmunkScript compileBenchmark(InputStream is, String name) {
		return ChipmunkVM.compile(is, name);
	}
	
	@State(Scope.Thread)
	public static class ChipmunkScripts {
		
		public CMethod countToAMillion;
		public CMethod fibonacci;
		
		public ChipmunkVM vm;
		
		@Setup(Level.Trial)
		public void compileSources() {
			// Run each script once to load/initialize modules, then retrieve the main methods for benchmarked execution
			ChipmunkVM vm = new ChipmunkVM();
			ChipmunkScript count = compileBenchmark(MathBenchmark.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
			vm.run(count);

			countToAMillion = (CMethod) count.getModules().get("benchmark").getNamespace().get("main");

			ChipmunkScript fib = compileBenchmark(MathBenchmark.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
			vm.run(fib);

			fibonacci = (CMethod) fib.getModules().get("benchmark").getNamespace().get("main");
		}
		
		@Setup(Level.Trial)
		public void initializeVM(){
			vm = new ChipmunkVM();
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object fibonacci(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		return vm.dispatch(scripts.fibonacci, null);
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionCVM(ChipmunkScripts scripts) {
		ChipmunkVM vm = scripts.vm;
		return vm.dispatch(scripts.countToAMillion, null);
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
