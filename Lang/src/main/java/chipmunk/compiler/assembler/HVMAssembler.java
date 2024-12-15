package chipmunk.compiler.assembler;

import myworld.hummingbird.Executable;
import myworld.hummingbird.Opcode;
import myworld.hummingbird.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HVMAssembler {

    private final Stack<HVMType> types = new Stack<>();
    private final RegisterAllocator registers = new RegisterAllocator();
    protected final Executable.Builder builder = new Executable.Builder();

    public void push(Object value){
        if(value instanceof Integer || value instanceof Long){
            types.push(HVMType.LONG);
        }else if(value instanceof Float || value instanceof Double){
            types.push(HVMType.DOUBLE);
        }else if(value instanceof String){
            types.push(HVMType.STRING_REF);
        }else{
            // TODO - invalid constant type
        }

        // TODO - Strings have to be encoded in the data section, with the address pushed as a constant.
        builder.appendOpcode(Opcodes.CONST(0, value)); // TODO - register allocation
    }

    public void add(){
        // TODO - for each Chipmunk opcode we have to inspect the HVM operand types and select the
        // opcode to use, possibly emitting conversion opcodes (converting longs to doubles, for example).
        var a = types.pop();
        var b = types.pop();
        if(a != b){
            promoteType(a, b);
        }

        switch (a){
            case LONG -> builder.appendOpcode(Opcodes.ADD(registers.pushRegister(), 0, 0));
            case DOUBLE -> builder.appendOpcode(Opcodes.DADD(registers.pushRegister(), 0, 0));
        }
    }

    protected void promoteType(HVMType a, HVMType b){
        if(a == HVMType.LONG && b == HVMType.DOUBLE){
            // TODO - promote register with a
            builder.appendOpcode(Opcodes.L2D(0, 0));
        }else if(a == HVMType.DOUBLE && b == HVMType.LONG){
            // TODO - promote register with b
            builder.appendOpcode(Opcodes.L2D(0, 0));
        }
    }

}
