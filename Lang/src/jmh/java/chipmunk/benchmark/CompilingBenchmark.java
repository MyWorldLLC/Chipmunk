/*
 * Copyright (C) 2022 MyWorld, LLC
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

import chipmunk.compiler.ChipmunkCompiler;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayInputStream;

public class CompilingBenchmark {

    @State(Scope.Thread)
    public static class Code {

        public byte[] source;

        @Setup(Level.Trial)
        public void initialize() throws Throwable {
            try(var is = CompilingBenchmark.class.getResourceAsStream("Mandelbrot.chp")){
                source = is.readAllBytes();
            }
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public Object compile(CompilingBenchmark.Code code) throws Throwable {
        var compiler = new ChipmunkCompiler();
        return compiler.compile(new ByteArrayInputStream(code.source), "Mandelbrot.chp");
    }

}
