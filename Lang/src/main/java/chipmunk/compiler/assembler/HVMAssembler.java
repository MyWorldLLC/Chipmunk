package chipmunk.compiler.assembler;

import myworld.hummingbird.Executable;
import myworld.hummingbird.Opcodes;

import java.nio.charset.StandardCharsets;

import static chipmunk.compiler.assembler.HVMType.*;

public class HVMAssembler {

    private final Operands operands = new Operands();
    protected final Executable.Builder builder = new Executable.Builder();
    private final Labeler labels = new Labeler();
    private final DebugTable debugTable = new DebugTable();

    public void push(Object value){
        Operand op = null;
        if(value instanceof Integer || value instanceof Long){
            op = operands.push(LONG);
        }else if(value instanceof Float || value instanceof Double){
            op = operands.push(DOUBLE);
        }else if(value instanceof String){
            op = operands.push(STRING_REF);
        }else{
            // TODO - invalid constant type
        }

        if(op.type() == HVMType.STRING_REF){
            // Strings have to be encoded in the data section, with the address pushed as a constant.
            value = builder.appendData(value.toString().getBytes(StandardCharsets.UTF_8));
        }
        builder.appendOpcode(Opcodes.CONST(op.register(), value));
    }

    public void add(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.ADD(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DADD(a.register(), a.register(), b.register()));
        }
    }

    public void sub(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.SUB(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DSUB(a.register(), a.register(), b.register()));
        }
    }

    public void mul(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.MUL(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DMUL(a.register(), a.register(), b.register()));
        }
    }

    public void div(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.DIV(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DDIV(a.register(), a.register(), b.register()));
        }
    }

    public void fdiv(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        // TODO - floor division
        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.DIV(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DDIV(a.register(), a.register(), b.register()));
        }
    }

    public void mod(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.REM(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DREM(a.register(), a.register(), b.register()));
        }
    }

    public void pow(){
        var b = operands.pop();
        var a = operands.pop();

        if(a != b){
            promoteType(a, b);
        }

        switch (a.type()){
            case LONG -> builder.appendOpcode(Opcodes.POW(a.register(), a.register(), b.register()));
            case DOUBLE -> builder.appendOpcode(Opcodes.DPOW(a.register(), a.register(), b.register()));
        }
    }

    public void inc(){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1));
                builder.appendOpcode(Opcodes.ADD(a.register(), a.register(), a.register() + 1));
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1.0d));
                builder.appendOpcode(Opcodes.DADD(a.register(), a.register(), a.register() + 1));
            }
        }
    }

    public void dec(){
        var a = operands.pop();

        switch (a.type()){
            case LONG -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1));
                builder.appendOpcode(Opcodes.SUB(a.register(), a.register(), a.register() + 1));
            }
            case DOUBLE -> {
                builder.appendOpcode(Opcodes.CONST(a.register() + 1, 1.0d));
                builder.appendOpcode(Opcodes.DSUB(a.register(), a.register(), a.register() + 1));
            }
        }
    }

    public Labeler labeler(){
        return labels;
    }

    public void onLine(int lineNumber){
        debugTable.onLine(lineNumber, 0); // TODO - ip
    }

    public void closeLine(){
        debugTable.closeLine(0); // TODO - ip
    }

    protected void promoteType(Operand a, Operand b){
        if(a.type() == LONG && b.type() == DOUBLE){
            // Promote register with a
            builder.appendOpcode(Opcodes.L2D(a.register(), a.register()));
        }else if(a.type() == DOUBLE && b.type() == LONG){
            // Promote register with b
            builder.appendOpcode(Opcodes.L2D(b.register(), b.register()));
        }
    }

}
