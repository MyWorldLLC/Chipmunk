package chipmunk.compiler.assembler;

// TODO - support permanently reserving registers (such as for constants)
public class RegisterAllocator {
    private int register;

    public int pushRegister(){
        var r = register;
        register++;
        return r;
    }

    public void popRegister(){
        register--;
    }
}
