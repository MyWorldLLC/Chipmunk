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

import java.io.InputStream;

import chipmunk.binary.BinaryModule;
import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.jvm.CompiledModule;
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

import chipmunk.ChipmunkVM;

public class MathBenchmark {
	
	public static CompiledModule compileBenchmark(InputStream is, String name) throws Throwable {
		ChipmunkCompiler compiler = new ChipmunkCompiler();
		BinaryModule module = compiler.compile(is, name)[0];

		ChipmunkVM vm = new ChipmunkVM();
		return vm.load(module);
	}
	
	@State(Scope.Thread)
	public static class ChipmunkScripts {
		
		public CompiledModule countToAMillion;
		public CompiledModule fibonacci;
		
		public ChipmunkVM vm;
		
		@Setup(Level.Trial)
		public void compileSources() throws Throwable {

			countToAMillion = compileBenchmark(MathBenchmark.class.getResourceAsStream("CountToAMillion.chp"), "countToAMillion");

			fibonacci = compileBenchmark(MathBenchmark.class.getResourceAsStream("Fibonacci.chp"), "fibonacci");

		}
		
		@Setup(Level.Trial)
		public void initializeVM(){
			vm = new ChipmunkVM();
		}
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object fibonacci(ChipmunkScripts scripts) throws Throwable {
		ChipmunkVM vm = scripts.vm;
		return vm.invoke(scripts.fibonacci, "main");
	}
	
	@Benchmark
	@BenchmarkMode(Mode.SampleTime)
	public Object countToOneMillionCVM(ChipmunkScripts scripts) throws Throwable {
		ChipmunkVM vm = scripts.vm;
		return vm.invoke(scripts.countToAMillion, "countToAMillion");
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


	public static void main(String[] args) throws RunnerException {

		Options opt = new OptionsBuilder()
				.include(MathBenchmark.class.getSimpleName())
				.forks(1)
				.build();

		new Runner(opt).run();

	}

}
