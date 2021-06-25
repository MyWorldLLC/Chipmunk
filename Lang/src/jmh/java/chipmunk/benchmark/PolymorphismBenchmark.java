/*
 * Copyright (C) 2021 MyWorld, LLC
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
import chipmunk.vm.ChipmunkVM;
import org.openjdk.jmh.annotations.*;

public class PolymorphismBenchmark {

    @State(Scope.Thread)
    public static class ChipmunkScripts {

        public ChipmunkScript polymorphic;
        public ChipmunkScript nonpolymorphic;

        public ChipmunkVM vm;

        @Setup(Level.Trial)
        public void initializeVM() throws Throwable {
            vm = new ChipmunkVM();
            polymorphic = vm.compileScript(PolymorphismBenchmark.class.getResourceAsStream("PolymorphicCalling.chp"), "polymorphic");
            nonpolymorphic = vm.compileScript(PolymorphismBenchmark.class.getResourceAsStream("NonpolymorphicCalling.chp"), "nonpolymorphic");
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public Object polymorphic(PolymorphismBenchmark.ChipmunkScripts scripts) throws Throwable {
        ChipmunkVM vm = scripts.vm;
        return vm.runAsync(scripts.polymorphic).get();
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    public Object nonpolymorphic(PolymorphismBenchmark.ChipmunkScripts scripts) throws Throwable {
        ChipmunkVM vm = scripts.vm;
        return vm.runAsync(scripts.nonpolymorphic).get();
    }
}
