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

package chipmunk.compiler.types;

import java.util.*;

import static chipmunk.compiler.types.BuiltinTypes.*;
import static chipmunk.compiler.types.Operation.binOp;
import static chipmunk.compiler.types.Operation.unary;

public class BuiltinOps {

    public static final String ADD = "+";
    public static final String SUB = "-";
    public static final String MULTIPLY = "*";
    public static final String POWER = "**";
    public static final String DIVIDE = "/";
    public static final String FLOOR_DIVIDE = "//";
    public static final String MODULO = "%";

    public static final String INC = "++";
    public static final String DEC = "--";

    public static final String AND = "&&";
    public static final String OR = "||";
    public static final String NOT = "!";
    public static final String LESS_THAN = "<";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN_OR_EQUAL = "<=";
    public static final String GREATER_THAN_OR_EQUAL = ">=";
    public static final String EQUALS = "==";

    public static final String BIN_NEG = "~";
    public static final String BIN_AND = "&";
    public static final String BIN_OR = "|";
    public static final String BIN_XOR = "^";
    public static final String LSHIFT = "<<";
    public static final String RSHIFT = ">>";
    public static final String URSHIFT = ">>>";

    protected final Map<String, List<Operation>> builtinOps;
    public BuiltinOps() {
        var ops = new HashMap<String, List<Operation>>();

        var BIN_NUMERIC = List.of(
                binOp(BYTE),
                binOp(SHORT),
                binOp(INTEGER),
                binOp(LONG),
                binOp(FLOAT),
                binOp(DOUBLE)
        );

        var BIN_INTEGRAL = List.of(
                binOp(BYTE),
                binOp(SHORT),
                binOp(INTEGER),
                binOp(LONG)
        );

        var UNARY_INTEGRAL = List.of(
                unary(BYTE),
                unary(SHORT),
                unary(INTEGER),
                unary(LONG)
        );

        var UNARY_NUMERIC = List.of(
                unary(BYTE),
                unary(SHORT),
                unary(INTEGER),
                unary(LONG),
                unary(FLOAT),
                unary(DOUBLE)
        );

        var COMPARABLE = List.of(
                binOp(BOOLEAN, BYTE),
                binOp(BOOLEAN, SHORT),
                binOp(BOOLEAN, INTEGER),
                binOp(BOOLEAN, LONG),
                binOp(BOOLEAN, FLOAT),
                binOp(BOOLEAN, DOUBLE),
                binOp(BOOLEAN, STRING)
        );

        // ================== Numeric Binary ==================
        ops.put(ADD, List.of(
                binOp(BYTE),
                binOp(SHORT),
                binOp(INTEGER),
                binOp(LONG),
                binOp(FLOAT),
                binOp(DOUBLE),
                binOp(STRING)));

        ops.put(SUB, List.of(
                binOp(BYTE),
                binOp(SHORT),
                binOp(INTEGER),
                binOp(LONG),
                binOp(FLOAT),
                binOp(DOUBLE),
                binOp(STRING)
        ));

        ops.put(MULTIPLY, BIN_NUMERIC);

        ops.put(POWER, BIN_NUMERIC);

        ops.put(DIVIDE, BIN_NUMERIC);

        ops.put(FLOOR_DIVIDE, BIN_NUMERIC);

        ops.put(MODULO, List.of(
                binOp(BYTE),
                binOp(SHORT),
                binOp(INTEGER),
                binOp(LONG),
                binOp(FLOAT),
                binOp(DOUBLE),
                binOp(STRING, STRING, LIST)
        ));

        ops.put(BIN_AND, BIN_INTEGRAL);
        ops.put(BIN_OR, BIN_INTEGRAL);
        ops.put(BIN_XOR, BIN_INTEGRAL);
        ops.put(LSHIFT, BIN_INTEGRAL);
        ops.put(RSHIFT, BIN_INTEGRAL);
        ops.put(URSHIFT, BIN_INTEGRAL);

        // ================== Numeric Unary ==================

        ops.put(INC, UNARY_NUMERIC);

        ops.put(DEC, UNARY_NUMERIC);

        ops.put(BIN_NEG, UNARY_INTEGRAL);

        // ================== Logical ==================
        ops.put(AND, List.of(
                binOp(BOOLEAN)
        ));

        ops.put(OR, List.of(
                binOp(BOOLEAN)
        ));

        ops.put(NOT, List.of(
                unary(BOOLEAN)
        ));

        // ================== Comparison ==================
        ops.put(LESS_THAN, COMPARABLE);
        ops.put(GREATER_THAN, COMPARABLE);
        ops.put(LESS_THAN_OR_EQUAL, COMPARABLE);
        ops.put(GREATER_THAN_OR_EQUAL, COMPARABLE);

        ops.put(EQUALS, List.of(
                binOp(BOOLEAN, BOOLEAN),
                binOp(BOOLEAN, BYTE),
                binOp(BOOLEAN, SHORT),
                binOp(BOOLEAN, INTEGER),
                binOp(BOOLEAN, LONG),
                binOp(BOOLEAN, FLOAT),
                binOp(BOOLEAN, DOUBLE),
                binOp(BOOLEAN, STRING),
                binOp(BOOLEAN, MAP),
                binOp(BOOLEAN, LIST),
                binOp(BOOLEAN, ANY)
        ));

        builtinOps = Collections.unmodifiableMap(ops);
    }

    public Optional<Operation> getOperation(String symbol, ObjectType... operands){
        var overloads = builtinOps.get(symbol);
        if(overloads == null) return Optional.empty();

        // Always try to exactly match first, because some operations may return a different type
        // than expected when promotions get involved. Splitting strict equality & promotable matching
        // into two separate phases guarantees that the closest operation always wins.
        for(var op : overloads){
            if(op.isExactMatch(operands)) return Optional.of(op);
        }

        for(var op : overloads){
            if(op.isPromotableMatch(operands)) return Optional.of(op);
        }

        return Optional.empty();
    }

}
