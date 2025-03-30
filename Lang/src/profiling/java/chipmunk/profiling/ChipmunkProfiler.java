/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

package chipmunk.profiling;

import chipmunk.vm.ChipmunkVM;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ChipmunkProfiler {

	private final ChipmunkVM vm = new ChipmunkVM();
	private final Map<String, Map<String, Callable<Object>>> programs = new HashMap<>();
	
	public static void main(String[] args) throws Throwable {
		var category = "chipmunk";
		var program = "countToAMillion";
		if(args.length > 0){
			if(args.length == 1){
				program = args[0];
			}else{
				category = args[0];
				program = args[1];
			}
		}

		var profiler = new ChipmunkProfiler();

		var chipmunkPrograms = new HashMap<String, Callable<Object>>();
		chipmunkPrograms.put("countToAMillion", profiler.load("countToAMillion", "CountToAMillion.chp"));
		//chipmunkPrograms.put("countingForLoop", profiler.load("countingForLoop", "CountingForLoop.chp"));
		chipmunkPrograms.put("fibonacci", profiler.load("fibonacci", "Fibonacci.chp"));
		//chipmunkPrograms.put("mandelbrot", profiler.load("mandelbrot", "Mandelbrot.chp"));
		//chipmunkPrograms.put("mathBench", profiler.load("mathBench", "MathBench.chp"));

		//chipmunkPrograms.put("polymorphism", profiler.load("polymorphism", "PolymorphicCalling.chp"));
		//chipmunkPrograms.put("nonpolymorphism", profiler.load("nonpolymorphism", "NonpolymorphicCalling.chp"));

		var javaPrograms = new HashMap<String, Callable<Object>>();
		javaPrograms.put("countToAMillion", TestPrograms.countOneMillion());
		javaPrograms.put("callOneMillion", TestPrograms.callOneMillion());
		javaPrograms.put("callAtOneMillion", TestPrograms.callAtOneMillion());
		javaPrograms.put("fibonacci30", TestPrograms.fibonacci30());
		javaPrograms.put("mathBench", TestPrograms.mathBench());
		javaPrograms.put("callJavaMethod", TestPrograms.callJavaMethod());
		javaPrograms.put("javaCountBigDecimal", TestPrograms.javaCountBigDecimal());
		javaPrograms.put("bytecodeCount", TestPrograms.bytecodeCount());

		profiler.programs.put("chipmunk", chipmunkPrograms);
		profiler.programs.put("java", javaPrograms);

		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		var selected = profiler.programs.get(category).get(program);
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = selected.call();
			long endTime = System.nanoTime();
			Thread.sleep(1000);
			
			System.out.println("Value: " + value + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

	private Callable<Object> load(String name, String resource) throws Exception {
		var script = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream(resource), name);
		return script::run;
	}

}
