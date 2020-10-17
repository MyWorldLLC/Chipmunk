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

import chipmunk.InvalidOpcodeChipmunk;
import chipmunk.binary.*;
import chipmunk.modules.runtime.CMethod;

import static chipmunk.Opcodes.*;

public class ChipmunkDisassembler {

	public static final String INDENTATION = "  ";

	public static String disassemble(BinaryModule module){
		StringBuilder builder = new StringBuilder();
		builder.append("module ");
		builder.append(module.getName());
		builder.append("\n\n");

		builder.append(INDENTATION);
		builder.append("Imports:\n");
		for(BinaryImport im : module.getImports()){

			String name = im.getName();
			boolean importAll = im.isImportAll();
			String[] aliases = im.getAliases();
			String[] symbols = im.getSymbols();

			builder.append(INDENTATION);
			if(importAll){
				builder.append(String.format("import %s.*", name));
			}else{
				builder.append(String.format("from %s import %s", name, String.join(",", symbols)));
				if(aliases != null && aliases.length > 0){
					builder.append(String.format(" as %s", String.join(",", aliases)));
				}
			}
			builder.append("\n");
		}
		builder.append("\n");

		final Object[] constantPool = module.getConstantPool();
		disassemble(constantPool, builder, INDENTATION);
		builder.append("\n");

		builder.append(INDENTATION);
		builder.append("Variables:\n");
		for(BinaryNamespace.Entry entry : module.getNamespace()){
			if(entry.getType() != FieldType.CLASS && entry.getType() != FieldType.METHOD){
				builder.append(INDENTATION);
				builder.append(INDENTATION);
				builder.append(entry.getName());
				builder.append("\n");
			}
		}
		builder.append("\n");

		if(module.getInitializer() != null){
			builder.append(INDENTATION);
			builder.append("<init>\n");
			builder.append(disassemble(module.getInitializer().getCode(), null, false, INDENTATION));
			builder.append("\n\n");
		}

		for(BinaryNamespace.Entry entry : module.getNamespace()){
			if(entry.getType() == FieldType.CLASS){
				disassemble(entry.getBinaryClass(), builder, INDENTATION);
			}else if(entry.getType() == FieldType.METHOD){
				disassemble(entry.getBinaryMethod(), builder, INDENTATION);
			}
		}

		return builder.toString();
	}

	public static void disassemble(BinaryClass cls, StringBuilder builder, String padding){
		builder.append(padding);
		builder.append("class ");
		builder.append(cls.getName());
		builder.append(":\n\n");

		padding = padding + INDENTATION;

		builder.append(padding);
		builder.append("Shared Attributes:\n");
		for(BinaryNamespace.Entry entry : cls.getSharedFields()){
			builder.append(padding);
			builder.append(INDENTATION);
			builder.append(entry.getName());
			builder.append("\n");
		}
		builder.append("\n");

		if(cls.getSharedInitializer() != null){
			builder.append(padding);
			builder.append("<shared init>\n");
			builder.append(disassemble(cls.getSharedInitializer().getCode(), null, false, padding));
			builder.append("\n");
		}

		builder.append(padding);
		builder.append("Instance Attributes:\n");
		for(BinaryNamespace.Entry entry : cls.getInstanceFields()){
			builder.append(padding);
			builder.append(INDENTATION);
			builder.append(entry.getName());
			builder.append("\n");
		}
		builder.append("\n");

		if(cls.getInstanceInitializer() != null){
			builder.append(padding);
			builder.append("<init>\n");
			builder.append(disassemble(cls.getInstanceInitializer().getCode(), null, false, padding));
			builder.append("\n\n");
		}

		for(BinaryNamespace.Entry entry : cls.getInstanceFields()){
			if(entry.getType() == FieldType.METHOD){
				builder.append(padding);
				builder.append(INDENTATION);
				builder.append("def ");
				builder.append(entry.getName());
				builder.append(":\n");
				builder.append(disassemble(entry.getBinaryMethod().getCode(), null, false, padding + INDENTATION));
				builder.append("\n\n");
			}
		}

	}

	private static void disassemble(Object[] constantPool, StringBuilder builder, String padding){
		builder.append(padding);
		builder.append("Constants:\n");

		String entryPadding = padding + "  ";
		for(int i = 0; i < constantPool.length; i++){
			builder.append(entryPadding);
			builder.append(i);
			builder.append(": ");
			builder.append(constantPool[i] == null ? "null" : constantPool[i].toString());

			if(constantPool[i] instanceof BinaryMethod){
				builder.append('\n');
				BinaryMethod method = (BinaryMethod) constantPool[i];
				builder.append(disassemble(method.getCode(), method.getConstantPool(), true, padding + "     "));
			}
			builder.append('\n');
		}
	}

	public static void disassemble(BinaryMethod method, StringBuilder builder, String padding){
		builder.append(padding);
		builder.append("method: ");
		builder.append(method.getDeclarationSymbol());
		builder.append("[Locals: ");
		builder.append(method.getLocalCount());
		builder.append(" ");
		builder.append(", Args: ");
		builder.append(method.getArgCount());
		builder.append("]:\n");

		byte[] codeSegment = method.getCode();
		disassemble(codeSegment, method.getConstantPool(), false, padding + INDENTATION);
	}
	
	public static String disassemble(byte[] codeSegment){
		return disassemble(codeSegment, null);
	}

	public static String disassemble(byte[] codeSegment, Object[] constantPool){
		return disassemble(codeSegment, constantPool, false, "");
	}
	
	private static String disassemble(byte[] codeSegment, Object[] constantPool, boolean isInnerMethod, String padding){
		StringBuilder builder = new StringBuilder();
		
		if(constantPool != null){
			builder.append(padding);
			builder.append("Constants:\n");
			if(isInnerMethod){
				builder.append(padding);
				builder.append("  ");
				builder.append("Shared\n");
			}else{
				String entryPadding = padding + "  ";
				for(int i = 0; i < constantPool.length; i++){
					builder.append(entryPadding);
					builder.append(i);
					builder.append(": ");
					builder.append(constantPool[i] == null ? "null" : constantPool[i].toString());
					
					if(constantPool[i] instanceof CMethod){
						builder.append('\n');
						CMethod method = (CMethod) constantPool[i];
						builder.append(disassemble(method.getCode().getCode(), method.getCode().getConstantPool(), true, padding + "     "));
					}
					builder.append('\n');
				}
			}
		}
		
		String codePadding = padding + INDENTATION;
		
		int ip = 0;
		while(ip < codeSegment.length){
			byte op = codeSegment[ip];

			builder.append(codePadding);
			builder.append(ip);
			builder.append(": ");
			
			switch(op){
			case ADD:
				builder.append("add");
				ip++;
				break;
			case SUB:
				builder.append("sub");
				ip++;
				break;
			case MUL:
				builder.append("mul");
				ip++;
				break;
			case DIV:
				builder.append("div");
				ip++;
				break;
			case FDIV:
				builder.append("fdiv");
				ip++;
				break;
			case MOD:
				builder.append("mod");
				ip++;
				break;
			case POW:
				builder.append("pow");
				ip++;
				break;
			case INC:
				builder.append("inc");
				ip++;
				break;
			case DEC:
				builder.append("dec");
				ip++;
				break;
			case POS:
				builder.append("pos");
				ip++;
				break;
			case NEG:
				builder.append("neg");
				ip++;
				break;
			case BXOR:
				builder.append("bxor");
				ip++;
				break;
			case BAND:
				builder.append("band");
				ip++;
				break;
			case BOR:
				builder.append("bor");
				ip++;
				break;
			case BNEG:
				builder.append("bneg");
				ip++;
				break;
			case LSHIFT:
				builder.append("lshift");
				ip++;
				break;
			case RSHIFT:
				builder.append("rshift");
				ip++;
				break;
			case URSHIFT:
				builder.append("urshift");
				ip++;
				break;
			case SETATTR:
				builder.append("setattr");
				ip++;
				break;
			case GETATTR:
				builder.append("getattr");
				ip++;
				break;
			case GETAT:
				builder.append("getat");
				ip++;
				break;
			case SETAT:
				builder.append("setat");
				ip++;
				break;
			case GETLOCAL:
				builder.append("getlocal ");
				builder.append(fetchByte(codeSegment, ip + 1));
				ip += 2;
				break;
			case SETLOCAL:
				builder.append("setlocal ");
				builder.append(fetchByte(codeSegment, ip + 1));
				ip += 2;
				break;
			case TRUTH:
				builder.append("truth");
				ip++;
				break;
			case NOT:
				builder.append("not");
				ip++;
				break;
			case AS:
				builder.append("as");
				ip++;
				break;
			case IF:
				builder.append("if ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case CALL:
				builder.append("call ");
				builder.append(fetchByte(codeSegment, ip + 1));
				ip += 2;
				break;
			case CALLAT:
				builder.append("callat ");
				builder.append(fetchByte(codeSegment, ip + 1));
				builder.append(' ');
				builder.append(fetchInt(codeSegment, ip + 2));
				ip += 6;
				break;
			case GOTO:
				builder.append("goto ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case THROW:
				builder.append("throw");
				ip++;
				break;
			case RETURN:
				builder.append("return");
				ip++;
				break;
			case POP:
				builder.append("pop");
				ip++;
				break;
			case DUP:
				builder.append("dup");
				ip++;
				break;
			case PUSH:
				builder.append("push ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case EQ:
				builder.append("eq");
				ip++;
				break;
			case GT:
				builder.append("gt");
				ip++;
				break;
			case LT:
				builder.append("lt");
				ip++;
				break;
			case GE:
				builder.append("ge");
				ip++;
				break;
			case LE:
				builder.append("le");
				ip++;
				break;
			case IS:
				builder.append("is");
				ip++;
				break;
			case INSTANCEOF:
				builder.append("instanceof");
				ip++;
				break;
			case ITER:
				builder.append("iter");
				ip++;
				break;
			case NEXT:
				builder.append("next ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case RANGE:
				builder.append("range ");
				builder.append(fetchByte(codeSegment, ip + 1));
				ip += 2;
				break;
			case LIST:
				builder.append("list ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case MAP:
				builder.append("map ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case INIT:
				builder.append("init");
				ip += 1;
				break;
			case GETMODULE:
				builder.append("getmodule ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case SETMODULE:
				builder.append("setmodule ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case INITMODULE:
				builder.append("initmodule ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			case IMPORT:
				builder.append("import ");
				builder.append(fetchInt(codeSegment, ip + 1));
				ip += 5;
				break;
			default:
				throw new InvalidOpcodeChipmunk(op);
			}
			builder.append('\n');
		}
		
		return builder.toString();
	}

	private static int fetchInt(byte[] instructions, int ip) {
		int b1 = instructions[ip] & 0xFF;
		int b2 = instructions[ip + 1] & 0xFF;
		int b3 = instructions[ip + 2] & 0xFF;
		int b4 = instructions[ip + 3] & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	private static byte fetchByte(byte[] instructions, int ip) {
		return instructions[ip];
	}
}
