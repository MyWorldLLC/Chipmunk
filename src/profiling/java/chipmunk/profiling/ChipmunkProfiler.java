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
import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.jvm.CompiledModule;
import chipmunk.modules.runtime.CMethod;
import chipmunk.runtime.ChipmunkModule;

public class ChipmunkProfiler {

	public static ChipmunkModule compileScript(InputStream is, String name) throws Throwable {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule module = compiler.compile(is, name)[0];

		ChipmunkVM vm = new ChipmunkVM();
		return vm.load(module);
	}
	
	public static void main(String[] args) throws Throwable {

		ChipmunkVM vm = new ChipmunkVM();
		ChipmunkModule countToAMillion = compileScript(ChipmunkProfiler.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");

		ChipmunkModule fibonacci = compileScript(ChipmunkProfiler.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
		
		System.out.println("Starting profiler. Press Ctrl-C to exit.");
		while(true){
			Object value;
			long startTime = System.nanoTime();
			value = vm.invoke(countToAMillion, "countToAMillion");
			long endTime = System.nanoTime();
			
			System.out.println("Value: " + value.toString() + ", Time: " + (endTime - startTime) / 1e9 + " seconds");
		}
		
	}

}
