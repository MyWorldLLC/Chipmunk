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

public class ChipmunkProfiler {

	
	public static void main(String[] args) throws Throwable {

		ChipmunkVM vm = new ChipmunkVM();
		ChipmunkScript countToAMillion = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
		ChipmunkScript countingForLoop = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("CountingForLoop.chp"), "countingForLoop");

		ChipmunkScript fibonacci = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
		ChipmunkScript mandelbrot = vm.compileScript(ChipmunkProfiler.class.getResourceAsStream("Mandelbrot.chp"), "mandelbrot");
		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.runAsync(countingForLoop).get();
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
