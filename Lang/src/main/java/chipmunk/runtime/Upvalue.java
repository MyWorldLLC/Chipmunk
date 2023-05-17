package chipmunk.runtime;

public class Upvalue {

    protected volatile Object value;

    public void set(Object value){
        this.value = value;
    }

    public Object get(){
        return value;
    }
}
