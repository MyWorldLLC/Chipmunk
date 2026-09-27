package chipmunk.runtime;

public class Upvalue {

    protected double value;

    public double set(double value){
        this.value = value;
        return value;
    }

    public double get(){
        return value;
    }
}
