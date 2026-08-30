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

package chipmunk.compiler;

import chipmunk.compiler.types.ObjectType;
import chipmunk.compiler.types.Operation;
import chipmunk.runtime.CRuntime;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.*;

import static chipmunk.compiler.BranchEmitter.branch;
import static chipmunk.compiler.ConversionEmitter.conversion;
import static chipmunk.compiler.OpEmitter.*;
import static chipmunk.compiler.types.BuiltinTypes.*;
import static java.lang.constant.ConstantDescs.*;

public class Intrinsics {

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
    public static final String DOUBLE_EQUALS = "==";
    public static final String EXCLAMATION_EQUALS = "!=";

    public static final String LBRACKET = "[";

    public static final String BIN_NEG = "~";
    public static final String BIN_AND = "&";
    public static final String BIN_OR = "|";
    public static final String BIN_XOR = "^";
    public static final String LSHIFT = "<<";
    public static final String RSHIFT = ">>";
    public static final String URSHIFT = ">>>";

    public static final String SET_AT = "setAt";
    public static final String IS = "is";

    protected static final Map<String, List<OpEmitter>> builtinOps;
    protected static final Map<ObjectType, List<ConversionEmitter>> typeConversions;
    protected static final Map<String, List<BranchEmitter>> branchIntrinsics;

    static {
        final var CD_Runtime = ClassDesc.of(CRuntime.class.getName());
        final var MT_iBinOp = MethodTypeDesc.of(CD_int, CD_int, CD_int);
        final var MT_lBinOp = MethodTypeDesc.of(CD_long, CD_long, CD_long);
        final var MT_fBinOp = MethodTypeDesc.of(CD_float, CD_float, CD_float);
        final var MT_dBinOp = MethodTypeDesc.of(CD_double, CD_double, CD_double);
        final var MT_bBinOp = MethodTypeDesc.of(CD_boolean, CD_boolean, CD_boolean);

        final var MT_iUnaryOp = MethodTypeDesc.of(CD_int, CD_int);
        final var MT_lUnaryOp = MethodTypeDesc.of(CD_long, CD_long);
        final var MT_fUnaryOp = MethodTypeDesc.of(CD_float, CD_float);
        final var MT_dUnaryOp = MethodTypeDesc.of(CD_double, CD_double);
        final var MT_bUnaryOp = MethodTypeDesc.of(CD_boolean, CD_boolean);

        final var MT_iCmpOp = MethodTypeDesc.of(CD_boolean, CD_int, CD_int);
        final var MT_lCmpOp = MethodTypeDesc.of(CD_boolean, CD_long, CD_long);
        final var MT_fCmpOp = MethodTypeDesc.of(CD_boolean, CD_float, CD_float);
        final var MT_dCmpOp = MethodTypeDesc.of(CD_boolean, CD_double, CD_double);
        final var MT_sCmpOp = MethodTypeDesc.of(CD_boolean, CD_String, CD_String);

        var ops = new HashMap<String, List<OpEmitter>>();

        ops.put(ADD, List.of(
                // Binary addition
                binOp(BYTE, CodeBuilder::iadd),
                binOp(SHORT, CodeBuilder::iadd),
                binOp(INT, CodeBuilder::iadd),
                binOp(LONG, CodeBuilder::ladd),
                binOp(FLOAT, CodeBuilder::fadd),
                binOp(DOUBLE, CodeBuilder::dadd),
                binOp(STRING, c -> c.invokevirtual(CD_String, "concat", MethodTypeDesc.of(CD_String, CD_String))),

                // Unary positive
                unary(BYTE, c -> c.invokestatic(CD_Runtime, "ipos", MT_iUnaryOp).i2b()),
                unary(SHORT, c -> c.invokestatic(CD_Runtime, "ipos", MT_iUnaryOp).i2s()),
                unary(INT, c -> c.invokestatic(CD_Runtime, "ipos", MT_iUnaryOp)),
                unary(LONG, c -> c.invokestatic(CD_Runtime, "lpos", MT_lUnaryOp)),
                unary(FLOAT, c -> c.invokestatic(CD_Runtime, "fpos", MT_fUnaryOp)),
                unary(DOUBLE, c -> c.invokestatic(CD_Runtime, "dpos", MT_dUnaryOp))
        ));

        ops.put(SUB, List.of(
                binOp(BYTE, CodeBuilder::isub),
                binOp(SHORT, CodeBuilder::isub),
                binOp(INT, CodeBuilder::isub),
                binOp(LONG, CodeBuilder::lsub),
                binOp(FLOAT, CodeBuilder::fsub),
                binOp(DOUBLE, CodeBuilder::dsub),
                binOp(STRING, c -> c.loadConstant("")
                        .invokevirtual(CD_String, "replaceAll", MethodTypeDesc.of(CD_String, CD_String, CD_String))),

                // Unary negative
                unary(BYTE, c -> c.ineg().i2b()),
                unary(SHORT, c -> c.ineg().i2s()),
                unary(INT, CodeBuilder::ineg),
                unary(LONG, CodeBuilder::lneg),
                unary(FLOAT, CodeBuilder::fneg),
                unary(DOUBLE, CodeBuilder::dneg)
        ));

        ops.put(MULTIPLY, List.of(
                binOp(BYTE, CodeBuilder::imul),
                binOp(SHORT, CodeBuilder::imul),
                binOp(INT, CodeBuilder::imul),
                binOp(LONG, CodeBuilder::lmul),
                binOp(FLOAT, CodeBuilder::fmul),
                binOp(DOUBLE, CodeBuilder::dmul)
        ));

        ops.put(POWER, List.of(
                binOp(BYTE, c -> c.invokestatic(CD_Runtime, "ipow", MT_iBinOp)),
                binOp(SHORT, c -> c.invokestatic(CD_Runtime, "ipow", MT_iBinOp)),
                binOp(INT, c -> c.invokestatic(CD_Runtime, "ipow", MT_iBinOp)),
                binOp(LONG, c -> c.invokestatic(CD_Runtime, "lpow", MT_lBinOp)),
                binOp(FLOAT, c -> c.invokestatic(CD_Runtime, "fpow", MT_fBinOp)),
                binOp(DOUBLE, c -> c.invokestatic(CD_Runtime, "dpow", MT_dBinOp))
        ));

        ops.put(DIVIDE, List.of(
                binOp(FLOAT, BYTE, c -> c.invokestatic(CD_Runtime, "idiv", MethodTypeDesc.of(CD_float, CD_int, CD_int))),
                binOp(FLOAT, SHORT, c -> c.invokestatic(CD_Runtime, "idiv", MethodTypeDesc.of(CD_float, CD_int, CD_int))),
                binOp(FLOAT, INT, c -> c.invokestatic(CD_Runtime, "idiv", MethodTypeDesc.of(CD_float, CD_int, CD_int))),
                binOp(DOUBLE, LONG, c -> c.invokestatic(CD_Runtime, "ldiv", MethodTypeDesc.of(CD_double, CD_long, CD_long))),
                binOp(FLOAT, CodeBuilder::fdiv),
                binOp(DOUBLE, CodeBuilder::ddiv)
        ));

        ops.put(FLOOR_DIVIDE, List.of(
                binOp(BYTE, c -> c.invokestatic(CD_Runtime, "ifdiv", MT_iBinOp)),
                binOp(SHORT, c -> c.invokestatic(CD_Runtime, "ifdiv", MT_iBinOp)),
                binOp(INT, c -> c.invokestatic(CD_Runtime, "ifdiv", MT_iBinOp)),
                binOp(LONG, c -> c.invokestatic(CD_Runtime, "lfdiv", MT_lBinOp)),
                binOp(FLOAT, c -> c.invokestatic(CD_Runtime, "ffdiv", MT_fBinOp)),
                binOp(DOUBLE, c -> c.invokestatic(CD_Runtime, "dfdiv", MT_dBinOp))
        ));

        ops.put(MODULO, List.of(
                binOp(BYTE, CodeBuilder::irem),
                binOp(SHORT, CodeBuilder::irem),
                binOp(INT, CodeBuilder::irem),
                binOp(LONG, CodeBuilder::lrem),
                binOp(FLOAT, CodeBuilder::frem),
                binOp(DOUBLE, CodeBuilder::drem),
                binOp(STRING, STRING, LIST, c -> c.invokestatic(CD_Runtime, "format",
                        MethodTypeDesc.of(CD_String, CD_String, ClassDesc.of(List.class.getName()))))
        ));

        ops.put(BIN_AND, List.of(
                binOp(BYTE, CodeBuilder::iand),
                binOp(SHORT, CodeBuilder::iand),
                binOp(INT, CodeBuilder::iand),
                binOp(LONG, CodeBuilder::iand)
        ));

        ops.put(BIN_OR, List.of(
                binOp(BYTE, CodeBuilder::ior),
                binOp(SHORT, CodeBuilder::ior),
                binOp(INT, CodeBuilder::ior),
                binOp(LONG, CodeBuilder::ior)
        ));

        ops.put(BIN_XOR, List.of(
                binOp(BYTE, CodeBuilder::ixor),
                binOp(SHORT, CodeBuilder::ixor),
                binOp(INT, CodeBuilder::ixor),
                binOp(LONG, CodeBuilder::ixor)
        ));

        ops.put(LSHIFT, List.of(
                binOp(BYTE, CodeBuilder::ishl),
                binOp(SHORT, CodeBuilder::ishl),
                binOp(INT, CodeBuilder::ishl),
                binOp(LONG, CodeBuilder::lshl)
        ));

        ops.put(RSHIFT, List.of(
                binOp(BYTE, CodeBuilder::ishr),
                binOp(SHORT, CodeBuilder::ishr),
                binOp(INT, CodeBuilder::ishr),
                binOp(LONG, CodeBuilder::lshr)
        ));

        ops.put(URSHIFT, List.of(
                binOp(BYTE, CodeBuilder::iushr),
                binOp(SHORT, CodeBuilder::iushr),
                binOp(INT, CodeBuilder::iushr),
                binOp(LONG, CodeBuilder::lushr)
        ));

        // ================== Numeric Unary ==================

        ops.put(INC, List.of(
                unary(BYTE, c -> c.invokestatic(CD_Runtime, "iinc", MT_iUnaryOp).i2b()),
                unary(SHORT, c -> c.invokestatic(CD_Runtime, "iinc", MT_iUnaryOp).i2s()),
                unary(INT, c -> c.invokestatic(CD_Runtime, "iinc", MT_iUnaryOp)),
                unary(LONG, c -> c.invokestatic(CD_Runtime, "linc", MT_lUnaryOp)),
                unary(FLOAT, c -> c.invokestatic(CD_Runtime, "finc", MT_fUnaryOp)),
                unary(DOUBLE, c -> c.invokestatic(CD_Runtime, "dinc", MT_dUnaryOp))
        ));

        ops.put(DEC, List.of(
                unary(BYTE, c -> c.invokestatic(CD_Runtime, "idec", MT_iUnaryOp).i2b()),
                unary(SHORT, c -> c.invokestatic(CD_Runtime, "idec", MT_iUnaryOp).i2s()),
                unary(INT, c -> c.invokestatic(CD_Runtime, "idec", MT_iUnaryOp)),
                unary(LONG, c -> c.invokestatic(CD_Runtime, "ldec", MT_lUnaryOp)),
                unary(FLOAT, c -> c.invokestatic(CD_Runtime, "fdec", MT_fUnaryOp)),
                unary(DOUBLE, c -> c.invokestatic(CD_Runtime, "ddec", MT_dUnaryOp))
        ));

        ops.put(BIN_NEG, List.of(
                unary(BYTE, c -> c.invokestatic(CD_Runtime, "ibneg", MT_iUnaryOp).i2b()),
                unary(SHORT, c -> c.invokestatic(CD_Runtime, "ibneg", MT_iUnaryOp).i2s()),
                unary(INT, c -> c.invokestatic(CD_Runtime, "ibneg", MT_iUnaryOp)),
                unary(LONG, c -> c.invokestatic(CD_Runtime, "lbneg", MT_lUnaryOp))
        ));

        // ================== Logical ==================
        ops.put(AND, List.of(
                binOp(BOOLEAN, c -> c.invokestatic(CD_Runtime, "and", MT_bBinOp))
        ));

        ops.put(OR, List.of(
                binOp(BOOLEAN, c -> c.invokestatic(CD_Runtime, "or", MT_bBinOp))
        ));

        ops.put(NOT, List.of(
                unary(BOOLEAN, c -> c.invokestatic(CD_Runtime, "not", MT_bUnaryOp))
        ));

        // ================== Comparison ==================
        ops.put(LESS_THAN, List.of(
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "ilt", MT_iCmpOp).i2b()),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "ilt", MT_iCmpOp).i2s()),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "ilt", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "llt", MT_iCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "flt", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "dlt", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "slt", MT_sCmpOp))
        ));

        ops.put(GREATER_THAN, List.of(
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "igt", MT_iCmpOp).i2b()),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "igt", MT_iCmpOp).i2s()),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "igt", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "lgt", MT_iCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "fgt", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "dgt", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "sgt", MT_sCmpOp))
        ));

        ops.put(LESS_THAN_OR_EQUAL, List.of(
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "ile", MT_iCmpOp).i2b()),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "ile", MT_iCmpOp).i2s()),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "ile", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "lle", MT_iCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "fle", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "dle", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "sle", MT_sCmpOp))
        ));
        ops.put(GREATER_THAN_OR_EQUAL, List.of(
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "ige", MT_iCmpOp).i2b()),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "ige", MT_iCmpOp).i2s()),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "ige", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "lge", MT_iCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "fge", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "dge", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "sge", MT_sCmpOp))
        ));

        ops.put(DOUBLE_EQUALS, List.of(
                binOp(BOOLEAN, BOOLEAN, c -> c.invokestatic(CD_Runtime, "beq", MethodTypeDesc.of(CD_boolean, CD_boolean, CD_boolean))),
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "ieq", MT_iCmpOp)),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "ieq", MT_iCmpOp)),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "ieq", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "leq", MT_lCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "feq", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "deq", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "objEq", MT_sCmpOp)),
                binOp(BOOLEAN, MAP, c -> c.invokestatic(CD_Runtime, "objEq", MethodTypeDesc.of(CD_boolean, CD_Object, CD_Object))),
                binOp(BOOLEAN, LIST, c -> c.invokestatic(CD_Runtime, "objEq", MethodTypeDesc.of(CD_boolean, CD_Object, CD_Object))),
                binOp(BOOLEAN, ANY, c -> c.invokestatic(CD_Runtime, "objEq", MethodTypeDesc.of(CD_boolean, CD_Object, CD_Object)))
        ));
        ops.put(EXCLAMATION_EQUALS, List.of(
                binOp(BOOLEAN, BOOLEAN, c -> c.invokestatic(CD_Runtime, "bneq", MethodTypeDesc.of(CD_boolean, CD_boolean, CD_boolean))),
                binOp(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "ineq", MT_iCmpOp)),
                binOp(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "ineq", MT_iCmpOp)),
                binOp(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "ineq", MT_iCmpOp)),
                binOp(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "lneq", MT_lCmpOp)),
                binOp(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "fneq", MT_fCmpOp)),
                binOp(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "dneq", MT_dCmpOp)),
                binOp(BOOLEAN, STRING, c -> c.invokestatic(CD_Runtime, "objNeq", MT_sCmpOp)),
                binOp(BOOLEAN, MAP, c -> c.invokestatic(CD_Runtime, "objNeq", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object))),
                binOp(BOOLEAN, LIST, c -> c.invokestatic(CD_Runtime, "objNeq", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object))),
                binOp(BOOLEAN, ANY, c -> c.invokestatic(CD_Runtime, "objNeq", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object)))
        ));

        ops.put(LBRACKET, List.of(
                binOp(ANY, LIST, INT, c -> c.invokeinterface(ClassDesc.of(List.class.getName()), "get", MethodTypeDesc.of(CD_Object, CD_int))),
                binOp(ANY, MAP, ANY, c -> c.invokeinterface(ClassDesc.of(Map.class.getName()), "get", MethodTypeDesc.of(CD_Object, CD_Object)))
        ));

        ops.put(SET_AT, List.of(
                tertiary(ANY, LIST, INT, ANY, c -> c.invokeinterface(ClassDesc.of(List.class.getName()), "set", MethodTypeDesc.of(CD_Object, CD_int, CD_Object))),
                tertiary(ANY, MAP, ANY, ANY, c -> c.invokeinterface(ClassDesc.of(Map.class.getName()), "put", MethodTypeDesc.of(CD_Object, CD_Object, CD_Object)))
        ));

        ops.put(IS, List.of(
                binOp(BOOLEAN, ANY, ANY, c -> {
                    var end = c.newLabel();
                    var ne = c.newLabel();
                    c.if_acmpne(ne)
                            .loadConstant(1)
                            .goto_(end)
                            .labelBinding(ne)
                            .loadConstant(0)
                            .labelBinding(end);
                })
        ));

        builtinOps = Collections.unmodifiableMap(ops);

        // ================== Automatic type conversions ==================
        var conversions = new IdentityHashMap<ObjectType, List<ConversionEmitter>>();

        // Boolean conversions
        conversions.put(BOOLEAN, List.of(
                conversion(BYTE,    BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2i", MethodTypeDesc.of(CD_int, CD_boolean)).i2b()),
                conversion(SHORT,   BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2i", MethodTypeDesc.of(CD_int, CD_boolean)).i2s()),
                conversion(INT,     BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2i", MethodTypeDesc.of(CD_int, CD_boolean))),
                conversion(LONG,    BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2i", MethodTypeDesc.of(CD_int, CD_boolean)).i2l()),
                conversion(STRING,  BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2s", MethodTypeDesc.of(CD_String, CD_boolean))),
                conversion(ANY,     BOOLEAN, c -> c.invokestatic(CD_Runtime, "b2a", MethodTypeDesc.of(CD_Boolean, CD_boolean)))
        ));

        // Byte conversions
        conversions.put(BYTE, List.of(
                conversion(BOOLEAN, BYTE, c -> c.invokestatic(CD_Runtime, "i2b", MethodTypeDesc.of(CD_boolean, CD_int))),
                conversion(SHORT,   BYTE, CodeBuilder::nop),
                conversion(INT,     BYTE, CodeBuilder::nop),
                conversion(LONG,    BYTE, CodeBuilder::i2l),
                conversion(FLOAT,   BYTE, CodeBuilder::i2f),
                conversion(DOUBLE,  BYTE, c -> c.i2f().i2d()),
                conversion(STRING,  BYTE, c -> c.invokestatic(CD_Runtime, "i2s", MethodTypeDesc.of(CD_String, CD_int))),
                conversion(ANY,     BYTE, c -> c.invokestatic(CD_Runtime, "b2a", MethodTypeDesc.of(CD_Byte, CD_byte)))
        ));

        // Short conversions
        conversions.put(SHORT, List.of(
                conversion(BOOLEAN, SHORT, c -> c.invokestatic(CD_Runtime, "i2b", MethodTypeDesc.of(CD_boolean, CD_int))),
                conversion(BYTE,    SHORT, CodeBuilder::i2b),
                conversion(INT,     SHORT, CodeBuilder::nop),
                conversion(LONG,    SHORT, CodeBuilder::i2l),
                conversion(FLOAT,   SHORT, CodeBuilder::i2f),
                conversion(DOUBLE,  SHORT, c -> c.i2f().i2d()),
                conversion(STRING,  SHORT, c -> c.invokestatic(CD_Runtime, "i2s", MethodTypeDesc.of(CD_String, CD_int))),
                conversion(ANY,     SHORT, c -> c.invokestatic(CD_Runtime, "s2a", MethodTypeDesc.of(CD_Short, CD_short)))
        ));

        // Int conversions
        conversions.put(INT, List.of(
                conversion(BOOLEAN, INT, c -> c.invokestatic(CD_Runtime, "i2b", MethodTypeDesc.of(CD_boolean, CD_int))),
                conversion(BYTE,    INT, CodeBuilder::i2b),
                conversion(SHORT,   INT, CodeBuilder::i2s),
                conversion(LONG,    INT, CodeBuilder::i2l),
                conversion(FLOAT,   INT, CodeBuilder::i2f),
                conversion(DOUBLE,  INT, CodeBuilder::i2d),
                conversion(STRING,  INT, c -> c.invokestatic(CD_Runtime, "i2s", MethodTypeDesc.of(CD_String, CD_int))),
                conversion(ANY,     INT, c -> c.invokestatic(CD_Runtime, "i2a", MethodTypeDesc.of(CD_Integer, CD_int)))
        ));

        // Long conversions
        conversions.put(LONG, List.of(
                conversion(BOOLEAN, LONG, c -> c.invokestatic(CD_Runtime, "l2b", MethodTypeDesc.of(CD_boolean, CD_long))),
                conversion(BYTE,    LONG, c -> c.l2i().i2b()),
                conversion(SHORT,   LONG, c -> c.l2i().i2s()),
                conversion(INT,     LONG, CodeBuilder::l2i),
                conversion(FLOAT,   LONG, CodeBuilder::l2f),
                conversion(DOUBLE,  LONG, CodeBuilder::l2d),
                conversion(STRING,  LONG, c -> c.invokestatic(CD_Runtime, "l2s", MethodTypeDesc.of(CD_String, CD_long))),
                conversion(ANY,     LONG, c -> c.invokestatic(CD_Runtime, "l2a", MethodTypeDesc.of(CD_Long, CD_long)))
        ));

        // Float conversions
        conversions.put(FLOAT, List.of(
                conversion(BOOLEAN, FLOAT, c -> c.invokestatic(CD_Runtime, "f2b", MethodTypeDesc.of(CD_boolean, CD_float))),
                conversion(BYTE,    FLOAT, c -> c.f2i().i2b()),
                conversion(SHORT,   FLOAT, c -> c.f2i().i2s()),
                conversion(INT,     FLOAT, CodeBuilder::f2i),
                conversion(DOUBLE,  FLOAT, CodeBuilder::f2d),
                conversion(STRING,  FLOAT, c -> c.invokestatic(CD_Runtime, "f2s", MethodTypeDesc.of(CD_String, CD_float))),
                conversion(ANY,     FLOAT, c -> c.invokestatic(CD_Runtime, "f2a", MethodTypeDesc.of(CD_Float, CD_float)))
        ));

        // Double conversions
        conversions.put(DOUBLE, List.of(
                conversion(BOOLEAN, DOUBLE, c -> c.invokestatic(CD_Runtime, "f2b", MethodTypeDesc.of(CD_boolean, CD_float))),
                conversion(BYTE,    DOUBLE, c -> c.d2i().i2b()),
                conversion(SHORT,   DOUBLE, c -> c.d2i().i2s()),
                conversion(INT,     DOUBLE, CodeBuilder::d2i),
                conversion(FLOAT,   DOUBLE, CodeBuilder::d2f),
                conversion(STRING,  DOUBLE, c -> c.invokestatic(CD_Runtime, "d2s", MethodTypeDesc.of(CD_String, CD_float))),
                conversion(ANY,     DOUBLE, c -> c.invokestatic(CD_Runtime, "d2a", MethodTypeDesc.of(CD_Double, CD_double)))
        ));

        // Unboxing conversions
        var CD_Number = ClassDesc.of(Number.class.getName());
        conversions.put(ANY, List.of(
                conversion(BOOLEAN, ANY, c -> c.checkcast(CD_Number)
                        .invokevirtual(CD_Number, "intValue", MethodTypeDesc.of(CD_int))
                        .invokestatic(CD_Runtime, "i2b",  MethodTypeDesc.of(CD_boolean, CD_int))
                ),
                conversion(BYTE,     ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "byteValue", MethodTypeDesc.of(CD_byte))),
                conversion(SHORT,    ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "shortValue", MethodTypeDesc.of(CD_short))),
                conversion(INT,      ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "intValue", MethodTypeDesc.of(CD_int))),
                conversion(LONG,     ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "longValue", MethodTypeDesc.of(CD_long))),
                conversion(FLOAT,    ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "floatValue", MethodTypeDesc.of(CD_float))),
                conversion(DOUBLE,   ANY, c -> c.checkcast(CD_Number).invokevirtual(CD_Number, "doubleValue", MethodTypeDesc.of(CD_double))),
                conversion(STRING,   ANY, c -> c.invokestatic(CD_Object, "toString", MethodTypeDesc.of(CD_String, CD_Object))),
                conversion(LIST,     ANY, c -> c.checkcast(ClassDesc.of(List.class.getName()))),
                conversion(MAP,      ANY, c -> c.checkcast(ClassDesc.of(Map.class.getName())))
        ));

        typeConversions = Collections.unmodifiableMap(conversions);

        var branches = new HashMap<String, List<BranchEmitter>>();

        branches.put(DOUBLE_EQUALS, List.of(
                branch(DOUBLE_EQUALS, BOOLEAN, CodeBuilder::if_icmpne),
                branch(DOUBLE_EQUALS, BYTE, CodeBuilder::if_icmpne),
                branch(DOUBLE_EQUALS, SHORT, CodeBuilder::if_icmpne),
                branch(DOUBLE_EQUALS, INT, CodeBuilder::if_icmpne),
                branch(DOUBLE_EQUALS, LONG, (c, l) -> c.lcmp().ifne(l)),
                branch(DOUBLE_EQUALS, FLOAT, (c, l) -> c.fcmpg().ifne(l)),
                branch(DOUBLE_EQUALS, DOUBLE, (c, l) -> c.dcmpg().ifne(l)),
                branch(DOUBLE_EQUALS, ANY, CodeBuilder::if_acmpne)
        ));

        branches.put(EXCLAMATION_EQUALS, List.of(
                branch(EXCLAMATION_EQUALS, BOOLEAN, CodeBuilder::if_icmpeq),
                branch(EXCLAMATION_EQUALS, BYTE, CodeBuilder::if_icmpeq),
                branch(EXCLAMATION_EQUALS, SHORT, CodeBuilder::if_icmpeq),
                branch(EXCLAMATION_EQUALS, INT, CodeBuilder::if_icmpeq),
                branch(EXCLAMATION_EQUALS, LONG, (c, l) -> c.lcmp().ifeq(l)),
                branch(EXCLAMATION_EQUALS, FLOAT, (c, l) -> c.fcmpg().ifeq(l)),
                branch(EXCLAMATION_EQUALS, DOUBLE, (c, l) -> c.dcmpg().ifeq(l)),
                branch(EXCLAMATION_EQUALS, ANY, CodeBuilder::if_acmpeq)
        ));

        branches.put(LESS_THAN, List.of(
                branch(LESS_THAN, BYTE, CodeBuilder::if_icmpge),
                branch(LESS_THAN, SHORT, CodeBuilder::if_icmpge),
                branch(LESS_THAN, INT, CodeBuilder::if_icmpge),
                branch(LESS_THAN, LONG, (c, l) -> c.lcmp().ifge(l)),
                branch(LESS_THAN, FLOAT, (c, l) -> c.fcmpg().ifge(l)),
                branch(LESS_THAN, DOUBLE, (c, l) -> c.dcmpg().ifge(l))
        ));

        branches.put(LESS_THAN_OR_EQUAL, List.of(
                branch(LESS_THAN_OR_EQUAL, BYTE, CodeBuilder::if_icmpgt),
                branch(LESS_THAN_OR_EQUAL, SHORT, CodeBuilder::if_icmpgt),
                branch(LESS_THAN_OR_EQUAL, INT, CodeBuilder::if_icmpgt),
                branch(LESS_THAN_OR_EQUAL, LONG, (c, l) -> c.lcmp().ifgt(l)),
                branch(LESS_THAN_OR_EQUAL, FLOAT, (c, l) -> c.fcmpg().ifgt(l)),
                branch(LESS_THAN_OR_EQUAL, DOUBLE, (c, l) -> c.dcmpg().ifgt(l))
        ));

        branches.put(GREATER_THAN, List.of(
                branch(GREATER_THAN, BYTE, CodeBuilder::if_icmple),
                branch(GREATER_THAN, SHORT, CodeBuilder::if_icmple),
                branch(GREATER_THAN, INT, CodeBuilder::if_icmple),
                branch(GREATER_THAN, LONG, (c, l) -> c.lcmp().ifle(l)),
                branch(GREATER_THAN, FLOAT, (c, l) -> c.fcmpg().ifle(l)),
                branch(GREATER_THAN, DOUBLE, (c, l) -> c.dcmpg().ifle(l))
        ));

        branches.put(GREATER_THAN_OR_EQUAL, List.of(
                branch(GREATER_THAN_OR_EQUAL, BYTE, CodeBuilder::if_icmplt),
                branch(GREATER_THAN_OR_EQUAL, SHORT, CodeBuilder::if_icmplt),
                branch(GREATER_THAN_OR_EQUAL, INT, CodeBuilder::if_icmplt),
                branch(GREATER_THAN_OR_EQUAL, LONG, (c, l) -> c.lcmp().iflt(l)),
                branch(GREATER_THAN_OR_EQUAL, FLOAT, (c, l) -> c.fcmpg().iflt(l)),
                branch(GREATER_THAN_OR_EQUAL, DOUBLE, (c, l) -> c.dcmpg().iflt(l))
        ));

        branchIntrinsics = Collections.unmodifiableMap(branches);
    }

    public static Optional<Operation> getOperation(String symbol, ObjectType... operands){
        return getEmitter(symbol, operands).map(OpEmitter::op);
    }

    public static Optional<OpEmitter> getEmitter(String symbol, ObjectType... operands){
        var intrinsics = builtinOps.get(symbol);
        if(intrinsics == null) return Optional.empty();

        // Always try to exactly match first, because some operations may return a different type
        // than expected when conversions get involved. Splitting strict equality & promotable matching
        // into two separate phases guarantees that the closest operation always wins.
        for(var emitter : intrinsics){
            if(emitter.op().isExactMatch(operands)) return Optional.of(emitter);
        }

        for(var emitter : intrinsics){
            if(emitter.op().isPromotableMatch(operands)) return Optional.of(emitter);
        }

        return Optional.empty();
    }

    public static Optional<ConversionEmitter> getConversion(ObjectType to, ObjectType from){
        var conversions = typeConversions.get(from);
        if(conversions == null) return Optional.empty();

        for(var emitter : conversions){
            if(from.isAssignableTo(emitter.from()) && to.isAssignableTo(emitter.to())) return Optional.of(emitter);
        }

        return Optional.empty();
    }

    public static Optional<BranchEmitter> getBranch(String operator, ObjectType operandType){
        var emitters = branchIntrinsics.get(operator);
        if(emitters == null) return Optional.empty();

        for(var emitter : emitters){
            if(operandType.isAssignableTo(emitter.operandType())) return Optional.of(emitter);
        }

        return Optional.empty();
    }

}
