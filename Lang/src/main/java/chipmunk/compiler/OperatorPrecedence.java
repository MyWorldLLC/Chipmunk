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

package chipmunk.compiler;

public class OperatorPrecedence {
	
	public static final int DOT_INDEX_CALL = 14;
	public static final int POW = 13;
	public static final int POST_INC_DEC = 12;
	public static final int PRE_OP = 11;
	public static final int MULT_DIV_MOD = 11;
	public static final int ADD_SUB = 10;
	public static final int SHIFT_L_R_RANGE = 9;
	public static final int LESSER_GREATER_THAN_INSTANCE_OF = 8;
	public static final int EQUAL_NEQUAL_IS = 7;
	public static final int BITAND = 6;
	public static final int BITXOR = 5;
	public static final int BITOR = 4;
	public static final int AND = 3;
	public static final int OR = 2;
	public static final int ASSIGN = 1;
}
