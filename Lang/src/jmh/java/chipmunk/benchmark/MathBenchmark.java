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

package chipmunk.benchmark;

import chipmunk.vm.ChipmunkScript;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import chipmunk.vm.ChipmunkVM;

public class MathBenchmark {
	
	@State(Scope.Thread)
	public static class ChipmunkScripts {
		
		public ChipmunkScript countToAMillion;
		public ChipmunkScript fibonacci;
		public ChipmunkScript mandelbrot;
		
		public ChipmunkVM vm;
		
		@Setup(Level.Trial)
		public void initializeVM() throws Throwable {
			vm = new ChipmunkVM();

			countToAMillion = vm.compileScript(MathBenchmark.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");
			fibonacci = vm.compileScript(MathBenchmark.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");
			mandelbrot = vm.compileScript(MathBenchmark.class.getResourceAsStream("Mandelbrot.chp"), "mandelbrot");
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object fibonacci(ChipmunkScripts scripts) throws Throwable {
		ChipmunkVM vm = scripts.vm;
		return vm.runAsync(scripts.fibonacci).get();
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionCVM(ChipmunkScripts scripts) throws Throwable {
		ChipmunkVM vm = scripts.vm;
		return vm.runAsync(scripts.countToAMillion).get();
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToAMillionJava(ChipmunkScripts scripts) {
		int x = 0;
		while(x < 1000000) {
			x = x + 1;
		}
		return x;
	}

	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object mandelbrot(ChipmunkScripts scripts) throws Throwable {
		ChipmunkVM vm = scripts.vm;
		return vm.runAsync(scripts.mandelbrot).get();
	}

}
