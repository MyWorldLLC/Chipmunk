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

import chipmunk.vm.ChipmunkScript;
import chipmunk.vm.ChipmunkVM;

import java.util.HashMap;

public class ChipmunkProfiler {

	
	public static void main(String[] args) throws Throwable {

		ChipmunkVM vm = new ChipmunkVM();
		var programs = new HashMap<String, ChipmunkScript>();
		programs.put("countToAMillion", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion"));
		programs.put("countingForLoop", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountingForLoop.chp"), "countingForLoop"));
		programs.put("fibonacci", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Fibonacci.chp"), "fibonacci"));
		programs.put("mandelbrot", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Mandelbrot.chp"), "mandelbrot"));
		programs.put("polymorphism", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("PolymorphicCalling.chp"), "polymorphism"));
		programs.put("nonpolymorphism", vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("NonpolymorphicCalling.chp"), "nonpolymorphism"));
		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");

		var program = programs.get("countToAMillion");
		if(args.length > 0){
			program = programs.get(args[0]);
		}

		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.runAsync(program).get();
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
