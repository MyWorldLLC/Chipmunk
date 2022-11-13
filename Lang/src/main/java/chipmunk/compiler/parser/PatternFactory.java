package chipmunk.compiler.parser;

import chipmunk.util.SeekableSequence;
import chipmunk.util.Sequence;

import java.util.List;
import java.util.function.Function;

public class PatternFactory<S, SEQ extends SeekableSequence<S>, T, R> {

    public record PartialPattern<S, SEQ extends SeekableSequence<S>, T, R>(List<T> pattern){

        public Pattern<S, SEQ, T, R> then(Function<SEQ, R> action){
            return new Pattern<>(pattern, action);
        }

    }

    public PartialPattern<S, SEQ, T, R> when(T... pattern){
        return new PartialPattern<>(List.of(pattern));
    }
}
