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

package chipmunk.runtime;

import chipmunk.vm.ModuleLoader;
import chipmunk.vm.hazel.HazelVM;
import chipmunk.vm.hazel.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CMapTest {
    protected HazelVM vm;
    protected CMap table;

    @BeforeEach
    public void setup(){
        vm = new HazelVM(new ModuleLoader());
        table = new CMap(vm, 100);
    }

    @Test
    public void insertAndGetKeys(){
        table.insert(1, 2);
        table.insert(3, 4);
        table.insert(5, 6);

        assertEquals(2, table.get(1));
        assertEquals(4, table.get(3));
        assertEquals(6, table.get(5));
    }

    @Test
    public void insertAndGetWithStringKeys(){
        var one = vm.fromHostValue("one");
        var three = vm.fromHostValue("three");
        var five = vm.fromHostValue("five");

        table.insert(one, 2);
        table.insert(three, 4);
        table.insert(five, 6);

        // Re-assign pointer values after insertion
        one = vm.fromHostValue("one");
        three = vm.fromHostValue("three");
        five = vm.fromHostValue("five");

        assertEquals(2, table.get(one));
        assertEquals(4, table.get(three));
        assertEquals(6, table.get(five));
    }

    @Test
    public void checkCount(){
        table.insert(1, 2);
        table.insert(3, 4);
        table.remove(3);
        table.remove(5);

        assertEquals(1, table.size());
    }

    @Test
    public void checkRemove(){
        table.insert(1, 2);
        table.insert(3, 4);

        table.remove(1);

        assertEquals(1, table.size());
        assertEquals(4, table.get(3));
        assertTrue(Value.isNullPointer(table.get(1)));
    }

    @Test
    public void checkContains(){
        table.insert(1, 2);

        assertTrue(table.contains(1));
        assertFalse(table.contains(3));
    }

    @Test
    public void checkClear(){
        table.insert(1, 2);
        table.insert(3, 4);

        table.clear();

        assertEquals(0, table.size());
        assertTrue(Value.isNullPointer(table.get(1)));
        assertFalse(table.contains(3));
    }

    @Test
    public void checkGrow(){

        for(int i = 1; i < 201; i++){
            table.insert(i, i);
        }

        assertEquals(200, table.size());

        for(int i = 1; i < 201; i++){
            assertEquals(i, table.get(i));
        }
    }

    @Test
    public void checkCopyTo(){
        var other = new CMap(vm, 10);

        table.insert(1, 2);
        table.insert(3, 4);

        assertTrue(table.capacity() > 0);

        table.copyTo(other);

        assertEquals(2, other.size());
        assertEquals(2, other.get(1));
        assertEquals(4, other.get(3));
    }

    @Test
    public void checkGetKeys(){
        table.insert(1, 2);
        table.insert(3, 4);

        var keys = table.keys();
        Arrays.sort(keys);
        assertArrayEquals(new double[]{1, 3}, keys);
    }

    @Test
    public void checkGetValues(){
        table.insert(1, 2);
        table.insert(3, 4);

        var values = table.values();
        Arrays.sort(values);
        assertArrayEquals(new double[]{2, 4}, values);
    }

    @Test
    public void checkCompact(){
        for(int i = 0; i < 200; i++){
            table.insert(i, i);
        }

        assertEquals(200, table.size());

        for(int i = 1; i < 101; i++){
            table.remove(i);
        }

        assertEquals(100, table.size());
        var keys = table.keys();
        var values = table.values();
        table.compact();

        var compactedKeys = table.keys();
        var compactedValues = table.values();

        Arrays.sort(keys);
        Arrays.sort(compactedKeys);
        Arrays.sort(values);
        Arrays.sort(compactedValues);

        assertArrayEquals(keys, compactedKeys);
        assertArrayEquals(values, compactedValues);
    }
}
