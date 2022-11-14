package chipmunk.util;

public interface Sequence<T> {
    T get();
    T peek();
    boolean hasMore();
}
