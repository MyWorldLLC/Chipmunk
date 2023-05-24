package chipmunk.runtime;

public class Upvalue {

    protected volatile Object value;

    public Object set(Object value){
        this.value = value;
        return value;
    }

    public Object get(){
        return value;
    }
}
