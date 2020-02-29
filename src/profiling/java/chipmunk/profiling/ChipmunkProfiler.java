package chipmunk.profiling;

import java.io.InputStream;

import chipmunk.ChipmunkScript;
import chipmunk.ChipmunkVM;
import chipmunk.modules.runtime.CMethod;

public class ChipmunkProfiler {
	
	public static ChipmunkScript compileScript(InputStream is, String name) throws Exception {
		return ChipmunkVM.compile(is, name);
	}
	
	public static void main(String[] args) throws Exception{

		ChipmunkVM vm = new ChipmunkVM();
		ChipmunkScript countToAMillion = compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
		vm.run(countToAMillion);

		ChipmunkScript fibonacci = compileScript(ChipmunkProfiler.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
		// Run once to initialize and load all modules
		vm.run(fibonacci);

		// After modules are initialized and one run has completed, get the main method and invoke repeatedly
		CMethod mainMethod = (CMethod) fibonacci
				.getModules()
				.get("profiling")
				.getNamespace()
				.get("main");
		

		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.dispatch(mainMethod, null);
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value.toString() + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
