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

package chipmunk.cli;

import chipmunk.cli.commands.Run;
import chipmunk.cli.commands.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(name = "chipmunk",
        mixinStandardHelpOptions = true,
        version = "chipmunk 1.0-alpha",
        description = "Chipmunk VM CLI",
        subcommands = {
            Run.class,
            Test.class
        }
)
public class ChipmunkCLI implements Callable<Integer> {

    public static final String CHIPMUNK_SRC_EXTENSION = "chp";
    public static final String CHIPMUNK_BIN_EXTENSION = "chpb";

    @Override
    public Integer call(){
        return new Run().call();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ChipmunkCLI()).execute(args);
        System.exit(exitCode);
    }
}
