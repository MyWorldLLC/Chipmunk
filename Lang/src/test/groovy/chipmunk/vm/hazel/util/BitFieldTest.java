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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BitFieldTest {

    private BitField bitField;

    @BeforeEach
    public void setupBitField(){
        bitField = new BitField(128);
    }

    @Test
    public void setAndClearBit(){
        bitField.set(62);
        assertTrue(bitField.isSet(62), "Failed to set bit");

        assertEquals(1L << 1, bitField.word(0), "Bit not set in the right place");

        bitField.clear(62);
        assertFalse(bitField.isSet(62), "Failed to clear bit");
    }

    @Test
    public void setEdgeBits(){
        // word 0 bit 0
        bitField.set(0);
        assertEquals(1L << 63, bitField.word(0), "Bit not set in the right place");
        bitField.clear(0);

        // word 0 bit 63
        bitField.set(63);
        assertEquals(1L, bitField.word(0), "Bit not set in the right place");
        bitField.clear(0);

        // word 1 bit 0
        bitField.set(64);
        assertEquals(1L << 63, bitField.word(1), "Bit not set in the right place");
        bitField.clear(64);

        // word 1 bit 63
        bitField.set(127);
        assertEquals(1L, bitField.word(1), "Bit not set in the right place");
        bitField.clear(127);
    }

}

