/*
 * Copyright (C) 2020 MyWorld, LLC
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

package chipmunk.compiler.assembler;

public class Opcodes {
	
	// Primitive Operations
	public static final byte ADD = 0x00;
	public static final byte SUB = 0x01;
	public static final byte MUL = 0x02;
	public static final byte DIV = 0x03;
	public static final byte FDIV = 0x04;
	public static final byte MOD = 0x05;
	public static final byte POW = 0x06;
	public static final byte INC = 0x07;
	public static final byte DEC = 0x08;
	public static final byte POS = 0x09;
	public static final byte NEG = 0x0A;
	public static final byte BXOR = 0x0B;
	public static final byte BAND = 0x0C;
	public static final byte BOR = 0x0D;
	public static final byte BNEG = 0x0E;
	public static final byte LSHIFT = 0x0F;
	public static final byte RSHIFT = 0x10;
	public static final byte URSHIFT = 0x11;

	// Stack operations
	public static final byte POP = 0x12;
	public static final byte DUP = 0x13;
	public static final byte SWAP = 0x14;
	public static final byte PUSH = 0x15;

	// Local operations
	public static final byte GETLOCAL = 0x16;
	public static final byte SETLOCAL = 0x18;

	// Flow operations
	public static final byte IF = 0x19;
	public static final byte CALL = 0x1A;
	public static final byte CALLAT = 0x1B;
	public static final byte GOTO = 0x1C;
	public static final byte THROW = 0x1D;
	public static final byte RETURN = 0x1E;

	// Comparison/Boolean operations
	public static final byte NOT = 0x21;
	public static final byte EQ = 0x22;
	public static final byte GT = 0x23;
	public static final byte LT = 0x24;
	public static final byte GE = 0x25;
	public static final byte LE = 0x26;
	public static final byte IS = 0x27;

	// Object operations
	public static final byte INSTANCEOF = 0x28;
	public static final byte SETATTR = 0x29;
	public static final byte GETATTR = 0x2A;
	public static final byte GETAT = 0x2B;
	public static final byte SETAT = 0x2C;
	public static final byte TRUTH = 0x2D;
	public static final byte AS = 0x2E;
	public static final byte ITER = 0x2F;
	public static final byte RANGE = 0x30;
	public static final byte LIST = 0x31;
	public static final byte MAP = 0x32;
	
	// Upvalues
	public static final byte INITUPVALUE = 0x36;
	public static final byte GETUPVALUE = 0x37;
	public static final byte SETUPVALUE = 0x38;

	// Method bindings
	public static final byte BIND = 0x39;
	
}
