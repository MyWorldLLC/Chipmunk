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
import chipmunk.vm.EntryPoint;

import java.util.HashMap;

public class ChipmunkProfiler {

	
	public static void main(String[] args) throws Throwable {

		ChipmunkVM vm = new ChipmunkVM();

		ChipmunkScript countToAMillion = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
		ChipmunkScript countingForLoop = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountingForLoop.chp"), "countingForLoop");

		ChipmunkScript fibonacci = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
		ChipmunkScript mandelbrot = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Mandelbrot.chp"), "mandelbrot");


		ChipmunkScript polymorphism = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("PolymorphicCalling.chp"), "polymorphism");
		ChipmunkScript nonpolymorphism = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("NonpolymorphicCalling.chp"), "nonpolymorphism");

		var programs = new HashMap<String, ChipmunkScript>();
		programs.put("countToAMillion", countToAMillion);
		programs.put("countingForLoop", countingForLoop);
		programs.put("fibonacci", fibonacci);
		programs.put("mandelbrot", mandelbrot);
		programs.put("polymorphism", polymorphism);
		programs.put("nonpolymorphism", nonpolymorphism);

		if(args.length == 0){
			System.out.println("No program specified, defaulting to countToAMillion");
			args = new String[]{"countToAMillion"};
		}else if(args.length > 1){
			System.out.println("You only need to supply one program. Extra arguments are ignored.");
		}

		var program = programs.get(args[0]);
		if(program == null){
			System.out.println("Program " + args[0] + " does not exist, exiting.");
			System.exit(0);
		}
		program.setEntryPoint(new EntryPoint("profiling", "main"));

		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = program.run();
			long endTime = System.nanoTime();

			Thread.sleep(1000);

			System.out.println("Value: " + value + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
