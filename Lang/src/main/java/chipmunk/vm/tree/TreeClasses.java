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

package chipmunk.vm.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class TreeClasses {
    

    public static class Suspension {
        int state;
        int value;
        NodePartial[] states;

        public Suspension(int state, NodePartial... states){
            this(state, 0, states);
        }

        public Suspension(int state, int value, NodePartial... states){
            this.state = state;
            this.value = value;
            this.states = states;
        }
    }

    public static class Context {
        int[] frame = new int[40];
        int framePtr = 0;
        int stackPtr = 0;
        int[] vars = new int[200];

        int interruptCounter = 0;
        volatile boolean interrupt;
        boolean _return;

        Object payload;
        Deque<Suspension> suspensions = new ArrayDeque<>();

        void markStackForCall(int locals){
            frame[framePtr] = stackPtr;
            framePtr++;
            stackPtr += locals;
        }

        void restoreStack(){
            framePtr--;
            stackPtr = frame[framePtr];
            _return = false;
        }

        int setLocal(int local, int value){
            vars[stackPtr + local] = value;
            return value;
        }

        long setLocalLong(int local, long value){
            vars[stackPtr + local] = (int) (value >> 32);
            vars[stackPtr + local + 1] = (int) value;
            return value;
        }

        int getLocal(int local){
            return vars[stackPtr + local];
        }

        long getLocalLong(int local){
            var a = vars[stackPtr + local];
            var b = vars[stackPtr + local + 1];
            return ((long) a << 32) & ((long) b & 0xFFFFFFFFL);
        }

        boolean checkInterrupt(){
            interruptCounter++;
            if(interruptCounter >= 100_000){
                return interrupt;
            }
            return false;
        }

        void repeat(NodePartial test, NodePartial body){
            int t = 0;
            try{
                t = test.execute(this, 0);
            }catch (Exception e){
                suspend(e, t, body, test);
            }

            while(t != 0 && !checkInterrupt()){
                try{
                    body.execute(this, 0);
                } catch (Exception e) {
                    suspend(e, t, body, test);
                }

                try{
                    t = test.execute(this, 0);
                } catch (Exception e) {
                    suspend(e, t, body, test);
                }
            }
        }

        void markForResume(Object state, NodePartial... resumables){
            // TODO push state + resumables.
        }

        void resume(Context ctx){
            var suspension = suspensions.pop();
            while(suspension != null){
                var value = suspension.value;
                // TODO - add support for exception handlers to suspensions. When executing a suspension,
                // if an exception is thrown walk back through the suspension stack and execute the nearest handler.
                for(int i = suspension.state; i < suspension.states.length; i++){
                    try {
                        value = suspension.states[i].execute(ctx, i);
                    }catch (Exception e){
                        ctx.suspend(e, value, Arrays.stream(suspension.states).skip(i - 1).toArray(NodePartial[]::new));
                    }
                }
                suspension = suspensions.pop();
            }

        }

        void suspendStateless(Throwable t, NodePartial... resumables) throws RuntimeException {
            suspend(t, 0, resumables);
        }

        void suspend(Throwable t, int state, NodePartial... resumables) throws RuntimeException {
            suspensions.add(new Suspension(state, resumables));
            throw new RuntimeException(t);
        }
    }

    public interface NodePartial {
        int execute(Context ctx, int state);
    }

    public interface StatelessNodePartial extends NodePartial {
        int execute(Context ctx);

        default int execute(Context ctx, int state){
            return execute(ctx);
        }
    }

    public interface Node {
        int execute(Context ctx);
    }

    public static class Const implements Node {
        int value;

        public Const(int v){
            value = v;
        }

        public Const(float f){
            value = Float.floatToIntBits(f);
        }

        @Override
        public int execute(Context ctx) {
            return value;
        }
    }

    public static class SetVar implements Node {
        public Node value;
        int v;

        public SetVar(int v){
            this.v = v;
        }

        @Override
        public int execute(Context ctx) {
            var r = value.execute(ctx);
            return ctx.setLocal(v, r);
        }
    }

    public static class GetVar implements Node {
        int v;

        public GetVar(int v){
            this.v = v;
        }

        @Override
        public int execute(Context ctx) {
            return ctx.getLocal(v);
        }
    }

    public static class Add implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) + r.execute(ctx);
        }
    }

    public static class Sub implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) - r.execute(ctx);
        }
    }

    public static class Mul implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            return l.execute(ctx) * r.execute(ctx);
        }
    }

    public static class FSub implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits(Float.intBitsToFloat(l.execute(ctx)) - Float.intBitsToFloat(r.execute(ctx)));
        }
    }

    public static class FDiv implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits(Float.intBitsToFloat(l.execute(ctx)) / Float.intBitsToFloat(r.execute(ctx)));
        }
    }

    public static class I2F implements Node {
        public Node i;

        @Override
        public int execute(Context ctx) {
            return Float.floatToIntBits((float) i.execute(ctx));
        }
    }

    public static class Iflt implements Node {
        public Node l, r;

        @Override
        public int execute(Context ctx) {
            int a;
            try {
                a = l.execute(ctx);
            } catch (Exception e) {
                ctx.payload = 10;
                throw e;
            }

            int b;
            try {
                b = r.execute(ctx);
            } catch (Exception e) {
                ctx.payload = 10;
                throw e;
            }
            return a < b ? 1 : 0;
        }
    }

    public static class If implements Node {
        public Node test;
        public Node _if;
        public Node _else;

        @Override
        public int execute(Context ctx) {
            int t;
            try{
                t = test.execute(ctx);
            }catch (Exception e){
                ctx.payload = 10;
                throw e;
            }
            if(t != 0){
                try{
                    return _if.execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
            }else if(_else != null){
                return _else.execute(ctx);
            }
            return 0;
        }
    }

    public static class Block implements Node {
        public Node[] body;

        @Override
        public int execute(Context ctx) {
            int result = 0;
            for(int i = 0; i < body.length; i++){
                try{
                    result = body[i].execute(ctx);
                }catch (Exception e){
                    ctx.payload = 10;
                    throw e;
                }
            }
            return result;
        }
    }

    public static class While implements Node {
        public Node test;
        public Node body;

        @Override
        public int execute(Context ctx) {
            return doBody(ctx, doTest(ctx, 0));
        }

        public int doTest(Context ctx, int prior){
            try{
                return test.execute(ctx);
            }catch (Exception e){
                ctx.suspendStateless(e, this::doBody);
            }
            return 0;
        }

        public int doBody(Context ctx, int t){
            while(t != 0 && !ctx.checkInterrupt()){
                try{
                    body.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
                }

                try{
                    t = test.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doBody);
                }
            }
            return 0;
        }
    }

    public static class For implements Node {
        public Node pre;
        public Node test;
        public Node body;
        public Node post;

        @Override
        public int execute(Context ctx) {
            doPre(ctx, 0);
            return doBody(ctx, doTest(ctx, 0));
        }

        public int doPre(Context ctx, int prior){
            if(pre != null){
                try{
                    pre.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
                }
            }
            return 0;
        }

        public int doTest(Context ctx, int prior){
            try{
                return test.execute(ctx);
            }catch (Exception e){
                ctx.suspendStateless(e, this::doBody);
            }
            return 0; // suspend() will rethrow so this will never be reached
        }

        public int doBody(Context ctx, int test){
            int t = test;
            while(t != 0 && !ctx.checkInterrupt()){
                try{
                    body.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doPost);
                }

                doPost(ctx, 0);
                t = doTest(ctx, 0);
            }
            return 0;
        }

        public int doPost(Context ctx, int prior){
            if(post != null){
                try{
                    post.execute(ctx);
                }catch (Exception e){
                    ctx.suspendStateless(e, this::doTest);
                }
            }
            return 0;
        }

    }

    public static class FunctionNode implements Node {
        public Node[] nodes;

        @Override
        public int execute(Context ctx) {
            for(int i = 0; i < nodes.length - 1; i++){
                var v = nodes[i].execute(ctx);
                if(ctx._return){
                    return v;
                }
            }
            return nodes[nodes.length - 1].execute(ctx);
        }
    }

    public static class CallNode implements Node {
        int locals = 0;
        public Node f;
        public Node[] args;

        public CallNode(int locals){
            this.locals = locals;
        }

        @Override
        public int execute(Context ctx) {
            for(int i = 0; i < args.length; i++){
                ctx.setLocal(i + locals, args[i].execute(ctx));
            }
            ctx.markStackForCall(locals);
            int result = f.execute(ctx);
            ctx.restoreStack();
            return result;
        }
    }

    public static class ReturnNode implements Node {
        public Node e;

        @Override
        public int execute(Context ctx) {
            var v = e.execute(ctx);
            ctx._return = true;
            return v;
        }
    }
}
