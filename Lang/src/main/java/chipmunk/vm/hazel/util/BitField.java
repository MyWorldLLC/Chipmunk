/*
 * Copyright (C) 2026 MyWorld, LLC
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

package chipmunk.vm.hazel.util;

public final class BitField {

    private static final long MASK = 1L << 63;

    private long[] words;

    public BitField(int initialSize){
        words = new long[wordsForSize(initialSize)];
    }

    public void resize(int newSize){
        var next = new long[wordsForSize(newSize)];
        System.arraycopy(words, 0, next, 0, Math.min(words.length, newSize));
        words = next;
    }

    public void set(int bitIndex){
        var wordIndex = bitIndex / 64;
        words[wordIndex] |= MASK >>> (bitIndex % 64);
    }

    public boolean isSet(int bitIndex){
        var wordIndex = bitIndex / 64;
        return (words[wordIndex] & (MASK >>> (bitIndex % 64))) != 0;
    }

    public void clear(int bitIndex){
        var wordIndex = bitIndex / 64;
        words[wordIndex] ^= MASK >>> (bitIndex % 64);
    }

    public void negate(){
        for(var word = 0; word < words.length; word++){
            words[word] = ~words[word];
        }
    }

    public long wordFor(int bitIndex){
        return words[bitIndex / 64];
    }

    public long word(int wordIndex){
        return words[wordIndex];
    }

    public int wordCount(){
        return words.length;
    }

    public int bitCount(){
        return words.length * 64;
    }

    public static int wordsForSize(int size){
        return size / 64 + (size % 64 != 0 ? 1 : 0);
    }

}

