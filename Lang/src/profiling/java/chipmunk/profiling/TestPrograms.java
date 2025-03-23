/*
 * Copyright (C) 2025 MyWorld, LLC
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

import chipmunk.vm.tree.Context;
import chipmunk.vm.tree.Node;
import chipmunk.vm.tree.nodes.*;

import java.util.concurrent.Callable;

public class TestPrograms {

    public static Callable<Object> added(Callable<Object> a, Callable<Object> b){

        return () -> {
            var resultA = a.call();
            var resultB = b.call();
            return ((Integer) resultA) + ((Integer) resultB);
        };
    }

    public static Callable<Object> countOneMillion(){

        var ctx = new Context();

        var initX = new SetVar(0);
        initX.value = new Const(0);

        var getX = new GetVar(0);

        var test = new Iflt();
        test.l = getX;
        test.r = new Const(1000000);

        var add = new Add();
        add.l = new GetVar(0);
        add.r = new Const(1);

        var body = new SetVar(0);
        body.value = add;

        var loop = new While();
        loop.test = test;
        loop.body = body;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }

    public static Callable<Object> fibonacci30(){

        var ctx = new Context();
        var fib = new FunctionNode();

        var getN = new GetVar(0);

        var test = new Iflt();
        test.l = getN;
        test.r = new Const(2);

        var _if = new If();
        _if.test = test;

        var bail = new ReturnNode();
        bail.e = getN;

        _if._if = bail;

        var subOne = new Sub();
        subOne.l = getN;
        subOne.r = new Const(1);

        var subTwo = new Sub();
        subTwo.l = getN;
        subTwo.r = new Const(2);

        var callOne = new CallNode(4, fib, subOne);

        var callTwo = new CallNode(4, fib, subTwo);

        var addFib = new Add();
        addFib.l = callOne;
        addFib.r = callTwo;

        var recursiveReturn = new ReturnNode();
        recursiveReturn.e = addFib;

        _if._else = recursiveReturn;

        fib.nodes = new Node[]{_if};

        var callFib = new CallNode(4, fib, new Const(30));
        return () -> callFib.execute(ctx);
    }

    public static Callable<Object> mathBench(){
        /*
        # int i = 0;
        # double x = 1.0;
        # for(i = 0; i<99999999; i++){
        #   x = (i + i + 2 * i + 1 - 0.379)/x;
        # }
         */

        var ctx = new Context();

        var initI = new SetVar(0); // i = 0
        initI.value = new Const(0);

        var initX = new SetVar(1); // x = 1.0f
        initX.value = new Const(1.0f);

        var getI = new GetVar(0);
        var getX = new GetVar(1);

        var test = new Iflt();
        test.l = getI;
        test.r = new Const(99999999); // i < 99999999

        var addIPlusI = new Add(); // i + i
        addIPlusI.l = getI;
        addIPlusI.r = getI;

        var twoI = new Mul(); // 2 * i
        twoI.l = new Const(2);
        twoI.r = getI;

        var addTwoI = new Add(); // i + i + 2 * i
        addTwoI.l = addIPlusI;
        addTwoI.r = twoI;

        var plus1 = new Add(); // i + i + 2 * i + 1
        plus1.l = addTwoI;
        plus1.r = new Const(1);

        var toFloat = new I2F(); // (float)(i + i + 2 * i + 1)
        toFloat.i = plus1;

        var fSub = new FSub(); // (float)(i + i + 2 * i + 1) - 0.379
        fSub.l = toFloat;
        fSub.r = new Const(0.379f);

        var div = new FDiv(); // ((float)(i + i + 2 * i + 1) - 0.379) / x
        div.l = fSub;
        div.r = getX;

        var setX = new SetVar(1); // x = ((float)(i + i + 2 * i + 1) - 0.379) / x
        setX.value = div;

        var addIPlus1 = new Add(); // i++
        addIPlus1.l = getI;
        addIPlus1.r = new Const(1);
        var incrementI = new SetVar(0);
        incrementI.value = addIPlus1;

        var loop = new For();
        loop.pre = initI;
        loop.test = test;
        loop.body = setX;
        loop.post = incrementI;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> Float.intBitsToFloat((int)program.execute(ctx));
    }

    public static Callable<Object> callOneMillion(){
        var ctx = new Context();

        var f = new FunctionNode();
        f.nodes = new Node[]{new Const(1)};

        var initX = new SetVar(0);
        initX.value = new Const(0);

        var getX = new GetVar(0);

        var test = new Iflt();
        test.l = getX;
        test.r = new Const(1000000);

        var callF = new CallNode(1, f, new Node[0]);

        var xPlus1 = new Add();
        xPlus1.l = new GetVar(0);
        xPlus1.r = callF;

        var setX = new SetVar(0);
        setX.value = xPlus1;

        var loop = new For();
        loop.pre = initX;
        loop.test = test;
        loop.body = setX;

        var program = new FunctionNode();
        program.nodes = new Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }
}
