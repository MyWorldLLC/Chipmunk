package chipmunk;

import static chipmunk.Opcodes.ADD;
import static chipmunk.Opcodes.AND;
import static chipmunk.Opcodes.AS;
import static chipmunk.Opcodes.BAND;
import static chipmunk.Opcodes.BNEG;
import static chipmunk.Opcodes.BOR;
import static chipmunk.Opcodes.BXOR;
import static chipmunk.Opcodes.CALL;
import static chipmunk.Opcodes.CALLAT;
import static chipmunk.Opcodes.DEC;
import static chipmunk.Opcodes.DIV;
import static chipmunk.Opcodes.DUP;
import static chipmunk.Opcodes.EQ;
import static chipmunk.Opcodes.FDIV;
import static chipmunk.Opcodes.GE;
import static chipmunk.Opcodes.GETAT;
import static chipmunk.Opcodes.GETATTR;
import static chipmunk.Opcodes.GETLOCAL;
import static chipmunk.Opcodes.GETMODULE;
import static chipmunk.Opcodes.GOTO;
import static chipmunk.Opcodes.GT;
import static chipmunk.Opcodes.IF;
import static chipmunk.Opcodes.INC;
import static chipmunk.Opcodes.INIT;
import static chipmunk.Opcodes.INSTANCEOF;
import static chipmunk.Opcodes.IS;
import static chipmunk.Opcodes.ITER;
import static chipmunk.Opcodes.LE;
import static chipmunk.Opcodes.LIST;
import static chipmunk.Opcodes.LSHIFT;
import static chipmunk.Opcodes.LT;
import static chipmunk.Opcodes.MAP;
import static chipmunk.Opcodes.MOD;
import static chipmunk.Opcodes.MUL;
import static chipmunk.Opcodes.NEG;
import static chipmunk.Opcodes.NEXT;
import static chipmunk.Opcodes.NOT;
import static chipmunk.Opcodes.OR;
import static chipmunk.Opcodes.POP;
import static chipmunk.Opcodes.POS;
import static chipmunk.Opcodes.POW;
import static chipmunk.Opcodes.PUSH;
import static chipmunk.Opcodes.RANGE;
import static chipmunk.Opcodes.RETURN;
import static chipmunk.Opcodes.RSHIFT;
import static chipmunk.Opcodes.SETAT;
import static chipmunk.Opcodes.SETATTR;
import static chipmunk.Opcodes.SETLOCAL;
import static chipmunk.Opcodes.SETMODULE;
import static chipmunk.Opcodes.SUB;
import static chipmunk.Opcodes.SWAP;
import static chipmunk.Opcodes.THROW;
import static chipmunk.Opcodes.TRUTH;
import static chipmunk.Opcodes.URSHIFT;

import chipmunk.modules.runtime.CClass;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CMethodCode;
import chipmunk.modules.runtime.CModule;

public class ChipmunkDisassembler {

	public static final String INDENTATION = "  ";

	public static String disassemble(CModule module){
		StringBuilder builder = new StringBuilder();
		builder.append("module ");
		builder.append(module.getName());
		builder.append("\n\n");

		final Object[] constantPool = module.getConstantsUnmodifiable().toArray();
		disassemble(constantPool, builder, INDENTATION);
		builder.append("\n");

		builder.append(INDENTATION);
		builder.append("Variables:\n");
		for(String varName : module.getNamespace().names()){
			builder.append(INDENTATION);
			builder.append(INDENTATION);
			builder.append(varName);
			builder.append("\n");
		}
		builder.append("\n");

		if(module.hasInitializer()){
			builder.append(INDENTATION);
			builder.append("<init>\n");
			builder.append(disassemble(module.getInitializer().getCode(), null, false, INDENTATION));
			builder.append("\n\n");
		}

		// TODO - Get & disassemble classes & methods in the namespace
		for(String name : module.getNamespace().names()){
			Object value = module.getNamespace().get(name);
			if(value instanceof CClass){
				CClass cls = (CClass) value;
				disassemble(cls, builder, INDENTATION);
			}else if(value instanceof CMethod){
				CMethod method = (CMethod) value;
			}
		}

		return builder.toString();
	}

	public static void disassemble(CClass cls, StringBuilder builder, String padding){
		builder.append(padding);
		builder.append("class ");
		builder.append(cls.getName());
		builder.append(":\n\n");

		padding = padding + INDENTATION;

		builder.append(padding);
		builder.append("Shared Attributes:\n");
		for(String varName : cls.getAttributes().names()){
			builder.append(padding);
			builder.append(INDENTATION);
			builder.append(varName);
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
		for(String varName : cls.getInstanceAttributes().names()){
			builder.append(padding);
			builder.append(INDENTATION);
			builder.append(varName);
			builder.append("\n");
		}
		builder.append("\n");

		if(cls.getInstanceInitializer() != null){
			builder.append(padding);
			builder.append("<init>\n");
			builder.append(disassemble(cls.getInstanceInitializer().getCode(), null, false, padding));
			builder.append("\n\n");
		}

		for(String varName : cls.getInstanceAttributes().names()){
			Object value = cls.getInstanceAttributes().get(varName);
			if(value instanceof CMethod){
				builder.append(padding);
				builder.append(INDENTATION);
				builder.append("def ");
				builder.append(varName);
				builder.append(":\n");
				builder.append(disassemble(((CMethod) value).getCode(), null, false, padding + INDENTATION));
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
			builder.append(constantPool[i].toString());

			if(constantPool[i] instanceof CMethod){
				builder.append('\n');
				CMethod method = (CMethod) constantPool[i];
				builder.append(disassemble(method.getCode(), method.getCode().getConstantPool(), true, padding + "     "));
			}
			builder.append('\n');
		}
	}
	
	public static String disassemble(CMethodCode codeSegment){
		return disassemble(codeSegment, null);
	}

	public static String disassemble(CMethodCode codeSegment, Object[] constantPool){
		return disassemble(codeSegment, constantPool, false, "");
	}
	
	private static String disassemble(CMethodCode codeSegment, Object[] constantPool, boolean isInnerMethod, String padding){
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
					builder.append(constantPool[i].toString());
					
					if(constantPool[i] instanceof CMethod){
						builder.append('\n');
						CMethod method = (CMethod) constantPool[i];
						builder.append(disassemble(method.getCode(), method.getCode().getConstantPool(), true, padding + "     "));
					}
					builder.append('\n');
				}
			}
		}
		
		String codePadding = padding + INDENTATION;
		
		int ip = 0;
		while(ip < codeSegment.getCode().length){
			byte op = codeSegment.getCode()[ip];

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
			case AND:
				builder.append("and");
				ip++;
				break;
			case OR:
				builder.append("or");
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
				builder.append(fetchByte(codeSegment.getCode(), ip + 1));
				ip += 2;
				break;
			case SETLOCAL:
				builder.append("setlocal ");
				builder.append(fetchByte(codeSegment.getCode(), ip + 1));
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
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case CALL:
				builder.append("call ");
				builder.append(fetchByte(codeSegment.getCode(), ip + 1));
				ip += 2;
				break;
			case CALLAT:
				builder.append("callat ");
				builder.append(fetchByte(codeSegment.getCode(), ip + 1));
				builder.append(' ');
				builder.append(fetchInt(codeSegment.getCode(), ip + 2));
				ip += 6;
				break;
			case GOTO:
				builder.append("goto ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
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
				builder.append("dup ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case SWAP:
				builder.append("swap ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				builder.append(' ');
				builder.append(fetchInt(codeSegment.getCode(), ip + 5));
				ip += 9;
				break;
			case PUSH:
				builder.append("push ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
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
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case RANGE:
				builder.append("range ");
				builder.append(fetchByte(codeSegment.getCode(), ip + 1));
				ip += 2;
				break;
			case LIST:
				builder.append("list ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case MAP:
				builder.append("map ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case INIT:
				builder.append("init");
				ip += 1;
				break;
			case GETMODULE:
				builder.append("getmodule ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
				ip += 5;
				break;
			case SETMODULE:
				builder.append("setmodule ");
				builder.append(fetchInt(codeSegment.getCode(), ip + 1));
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
