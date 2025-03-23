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

import chipmunk.vm.tree.TreeClasses;

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

        var ctx = new TreeClasses.Context();

        var initX = new TreeClasses.SetVar(0);
        initX.value = new TreeClasses.Const(0);

        var getX = new TreeClasses.GetVar(0);

        var test = new TreeClasses.Iflt();
        test.l = getX;
        test.r = new TreeClasses.Const(1000000);

        var add = new TreeClasses.Add();
        add.l = new TreeClasses.GetVar(0);
        add.r = new TreeClasses.Const(1);

        var body = new TreeClasses.SetVar(0);
        body.value = add;

        var loop = new TreeClasses.While();
        loop.test = test;
        loop.body = body;

        var program = new TreeClasses.FunctionNode();
        program.nodes = new TreeClasses.Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }

    public static Callable<Object> fibonacci30(){

        var ctx = new TreeClasses.Context();
        var fib = new TreeClasses.FunctionNode();

        var getN = new TreeClasses.GetVar(0);

        var test = new TreeClasses.Iflt();
        test.l = getN;
        test.r = new TreeClasses.Const(2);

        var _if = new TreeClasses.If();
        _if.test = test;

        var bail = new TreeClasses.ReturnNode();
        bail.e = getN;

        _if._if = bail;

        var subOne = new TreeClasses.Sub();
        subOne.l = getN;
        subOne.r = new TreeClasses.Const(1);

        var subTwo = new TreeClasses.Sub();
        subTwo.l = getN;
        subTwo.r = new TreeClasses.Const(2);

        var callOne = new TreeClasses.CallNode(2);
        callOne.f = fib;
        callOne.args = new TreeClasses.Node[]{subOne};

        var callTwo = new TreeClasses.CallNode(2);
        callTwo.f = fib;
        callTwo.args = new TreeClasses.Node[]{subTwo};

        var addFib = new TreeClasses.Add();
        addFib.l = callOne;
        addFib.r = callTwo;

        var recursiveReturn = new TreeClasses.ReturnNode();
        recursiveReturn.e = addFib;

        _if._else = recursiveReturn;

        fib.nodes = new TreeClasses.Node[]{_if};

        var callFib = new TreeClasses.CallNode(2);
        callFib.f = fib;
        callFib.args = new TreeClasses.Node[]{new TreeClasses.Const(30)};
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

        var ctx = new TreeClasses.Context();

        var initI = new TreeClasses.SetVar(0); // i = 0
        initI.value = new TreeClasses.Const(0);

        var initX = new TreeClasses.SetVar(1); // x = 1.0f
        initX.value = new TreeClasses.Const(1.0f);

        var getI = new TreeClasses.GetVar(0);
        var getX = new TreeClasses.GetVar(1);

        var test = new TreeClasses.Iflt();
        test.l = getI;
        test.r = new TreeClasses.Const(99999999); // i < 99999999

        var addIPlusI = new TreeClasses.Add(); // i + i
        addIPlusI.l = getI;
        addIPlusI.r = getI;

        var twoI = new TreeClasses.Mul(); // 2 * i
        twoI.l = new TreeClasses.Const(2);
        twoI.r = getI;

        var addTwoI = new TreeClasses.Add(); // i + i + 2 * i
        addTwoI.l = addIPlusI;
        addTwoI.r = twoI;

        var plus1 = new TreeClasses.Add(); // i + i + 2 * i + 1
        plus1.l = addTwoI;
        plus1.r = new TreeClasses.Const(1);

        var toFloat = new TreeClasses.I2F(); // (float)(i + i + 2 * i + 1)
        toFloat.i = plus1;

        var fSub = new TreeClasses.FSub(); // (float)(i + i + 2 * i + 1) - 0.379
        fSub.l = toFloat;
        fSub.r = new TreeClasses.Const(0.379f);

        var div = new TreeClasses.FDiv(); // ((float)(i + i + 2 * i + 1) - 0.379) / x
        div.l = fSub;
        div.r = getX;

        var setX = new TreeClasses.SetVar(1); // x = ((float)(i + i + 2 * i + 1) - 0.379) / x
        setX.value = div;

        var addIPlus1 = new TreeClasses.Add(); // i++
        addIPlus1.l = getI;
        addIPlus1.r = new TreeClasses.Const(1);
        var incrementI = new TreeClasses.SetVar(0);
        incrementI.value = addIPlus1;

        var loop = new TreeClasses.For();
        loop.pre = initI;
        loop.test = test;
        loop.body = setX;
        loop.post = incrementI;

        var program = new TreeClasses.FunctionNode();
        program.nodes = new TreeClasses.Node[]{initX, loop, getX};

        return () -> Float.intBitsToFloat(program.execute(ctx));
    }

    public static Callable<Object> callOneMillion(){
        var ctx = new TreeClasses.Context();

        var f = new TreeClasses.FunctionNode();
        f.nodes = new TreeClasses.Node[]{new TreeClasses.Const(1)};

        var initX = new TreeClasses.SetVar(0);
        initX.value = new TreeClasses.Const(0);

        var getX = new TreeClasses.GetVar(0);

        var test = new TreeClasses.Iflt();
        test.l = getX;
        test.r = new TreeClasses.Const(1000000);

        var callF = new TreeClasses.CallNode(1);
        callF.args = new TreeClasses.Node[0];
        callF.f = f;

        var xPlus1 = new TreeClasses.Add();
        xPlus1.l = new TreeClasses.GetVar(0);
        xPlus1.r = callF;

        var setX = new TreeClasses.SetVar(0);
        setX.value = xPlus1;

        var loop = new TreeClasses.For();
        loop.pre = initX;
        loop.test = test;
        loop.body = setX;

        var program = new TreeClasses.FunctionNode();
        program.nodes = new TreeClasses.Node[]{initX, loop, getX};

        return () -> program.execute(ctx);
    }
}
