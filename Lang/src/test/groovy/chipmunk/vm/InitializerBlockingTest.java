/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm;

import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.vm.hazel.EntryPoint;
import chipmunk.vm.invoke.SecurityMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitializerBlockingTest {

    private ChipmunkVM vm;
    private List<ChipmunkScript> scripts;

    @BeforeEach
    public void setupVM(){
        vm = new ChipmunkVM(SecurityMode.DENYING, 2);
        var compiler = new ChipmunkCompiler();
        var compiled = compiler.compile(getClass().getResourceAsStream("/chipmunk/InitializerBlockingTest.chp"), "InitializerBlockingTest.chp");

        scripts = new ArrayList<>();
        for(int i = 0; i < 250; i++){
            var script = vm.compileScript(new EntryPoint("test", "main"), compiled);
            script.setErrorHandler((s, t, f) -> {
                f.completeExceptionally(t);
                throw new RuntimeException(t);
            });
            scripts.add(script);
        }
    }

    @Test
    public void testInitOrderUnderLoad() throws ExecutionException, InterruptedException, TimeoutException {
        var futures = new ArrayList<CompletableFuture<Object>>();
        for(ChipmunkScript script : scripts){
            futures.add(vm.run(script));
        }

        for(CompletableFuture<Object> future : futures){
            assertEquals(10.0, future.get());
        }

        vm.stop();
    }
}
