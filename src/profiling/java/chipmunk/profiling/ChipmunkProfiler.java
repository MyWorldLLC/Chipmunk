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
