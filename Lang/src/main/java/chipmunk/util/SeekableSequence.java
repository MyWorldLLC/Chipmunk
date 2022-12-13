package chipmunk.util;

public interface SeekableSequence<T> extends Sequence<T>, Visitor<T> {

    SeekableSequence<T> seek(int index);

    default T get(int skip){
        return seek(skip).get();
    }

}
