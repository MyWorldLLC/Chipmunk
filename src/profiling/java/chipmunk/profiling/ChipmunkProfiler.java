package chipmunk.profiling;

import java.io.InputStream;

import chipmunk.ChipmunkScript;
import chipmunk.ChipmunkVM;

public class ChipmunkProfiler {
	
	public static ChipmunkScript compileScript(InputStream is, String name) throws Exception {
		return ChipmunkVM.compile(is, name);
	}
	
	public static void main(String[] args) throws Exception{
		
		ChipmunkScript countToAMillion = compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
		countToAMillion.setEntryCall("profiling", "countToAMillion");
		ChipmunkVM vm = new ChipmunkVM();
		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.run(countToAMillion);
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value.toString() + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
