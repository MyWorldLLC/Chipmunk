package chipmunk.modules.lang;

import java.util.HashMap;
import java.util.Map;

import static chipmunk.Opcodes.*;
import chipmunk.ChipmunkContext;
import chipmunk.ExceptionChipmunk;
import chipmunk.InvalidOpcodeChipmunk;

public class CMethod extends CObject {
	
	protected CMethod outer;
	protected int argCount;
	protected int localCount;
	protected Map<String, Integer> localNames;
	
	protected byte[] instructions;
	protected CCode code;
	
	public CMethod(){
		super();
		localNames = new HashMap<String, Integer>();
	}

	public CMethod getOuter(){
		return outer;
	}
	
	public void setOuter(CMethod outer){
		this.outer = outer;
	}
	
	public int getArgCount(){
		return argCount;
	}
	
	public void setArgCount(int count){
		argCount = count;
	}
	
	public CCode getCode(){
		return code;
	}
	
	public void setCode(CCode code){
		this.code = code;
		instructions = code.getCode();
	}

	public int getLocalIndex(String name){
		
		Integer index = localNames.get(name);
		
		if(index != null){
			return index;
		}else{
			return -1;
		}
		
	}
	
	public void setLocalIndex(String name, int index){
		localNames.put(name, index);
	}
	
	@Override
	public CObject __call__(ChipmunkContext context, int paramCount, boolean resuming){
		
		int ip = 0;
		CObject[] locals;
		
		if(resuming){
			// handle resume
		}else{
			locals = new CObject[localCount];
			// TODO - pop args
		}
		
		while(true){
			
			byte op = instructions[ip];
			
			CObject rh;
			CObject lh;
			CObject ins;
			
			switch(op){
			
			case ADD:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__plus__(rh));
				ip++;
				break;
			case SUB:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__minus__(rh));
				ip++;
				break;
			case MUL:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__mul__(rh));
				ip++;
				break;
			case DIV:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__div__(rh));
				ip++;
				break;
			case FDIV:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__fdiv__(rh));
				ip++;
				break;
			case MOD:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__rem__(rh));
				ip++;
				break;
			case POW:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__pow__(rh));
				ip++;
				break;
			case INC:
				lh = context.pop();
				context.push(lh.__inc__());
				ip++;
				break;
			case DEC:
				lh = context.pop();
				context.push(lh.__dec__());
				ip++;
				break;
			case POS:
				lh = context.pop();
				context.push(lh.__pos__());
				ip++;
				break;
			case NEG:
				lh = context.pop();
				context.push(lh.__neg__());
				ip++;
				break;
			case AND:
				rh = context.pop();
				lh = context.pop();
				if(lh.__truth__()){
					if(rh.__truth__()){
						context.push(new CBoolean(true));
					}else{
						context.push(new CBoolean(false));
					}
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case OR:
				rh = context.pop();
				lh = context.pop();
				if(lh.__truth__() || rh.__truth__()){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case BXOR:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__bxor__(rh));
				ip++;
				break;
			case BAND:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__band__(rh));
				ip++;
				break;
			case BOR:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__bor__(rh));
				ip++;
				break;
			case BNEG:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__bneg__());
				ip++;
				break;
			case LSHIFT:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__lshift__(rh));
				ip++;
				break;
			case RSHIFT:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__rshift__(rh));
				ip++;
				break;
			case URSHIFT:
				rh = context.pop();
				lh = context.pop();
				context.push(lh.__urshift__(rh));
				ip++;
				break;
			case SETATTR:
				rh = context.pop();
				lh = context.pop();
				ins = context.pop();
				ins.__setAttr__(lh, rh);
				context.push(ins);
				ip++;
				break;
			case GETATTR:
				lh = context.pop();
				ins = context.pop();
				context.push(ins.__getAttr__(lh));
				ip++;
				break;
			case GETAT:
				lh = context.pop();
				ins = context.pop();
				context.push(ins.__getAt__(lh));
				ip++;
				break;
			case SETAT:
				rh = context.pop();
				lh = context.pop();
				ins = context.pop();
				context.push(ins.__setAt__(lh, rh));
				ip++;
				break;
			case TRUTH:
				rh = context.pop();
				context.push(new CBoolean(rh.__truth__()));
				ip++;
				break;
			case AS:
				lh = context.pop();
				ins = context.pop();
				context.push(ins.__as__(lh));
				ip++;
				break;
			case NEW:
				// TODO
				break;
			case IF:
				ins = context.pop();
				if(!ins.__truth__()){
					ip = fetchInt(ip + 1);
				}
				ip += 5;
				break;
			case CALL:
				// TODO
				int args = fetchInt(ip + 1);
				ins = context.pop();
				context.push(ins.__call__(context, args, false));
				ip += 5;
				break;
			case GOTO:
				int gotoIndex = fetchInt(ip + 1);
				ip += gotoIndex;
				break;
			case THROW:
				ins = context.pop();
				throw new ExceptionChipmunk(ins);
			case RETURN:
				ins = context.pop();
				return ins;
			case POP:
				ins = context.pop();
				ip++;
				break;
			case DUP:
				int dupIndex = fetchInt(ip + 1);
				context.dup(dupIndex);
				ip += 5;
				break;
			case SWAP:
				int swapIndex1 = fetchInt(ip + 1);
				int swapIndex2 = fetchInt(ip + 5);
				context.swap(swapIndex1, swapIndex2);
				ip += 9;
				break;
			case PUSH:
				// TODO
				break;
			case EQ:
				lh = context.pop();
				rh = context.pop();
				if(lh.__compare__(rh) == 0){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case GT:
				lh = context.pop();
				rh = context.pop();
				if(lh.__compare__(rh) > 0){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case LT:
				lh = context.pop();
				rh = context.pop();
				if(lh.__compare__(rh) < 0){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case GE:
				lh = context.pop();
				rh = context.pop();
				if(lh.__compare__(rh) >= 0){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case LE:
				lh = context.pop();
				rh = context.pop();
				if(lh.__compare__(rh) <= 0){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			case IS:
				lh = context.pop();
				rh = context.pop();
				if(lh == rh){
					context.push(new CBoolean(true));
				}else{
					context.push(new CBoolean(false));
				}
				ip++;
				break;
			default:
				throw new InvalidOpcodeChipmunk(op);
			}
			
			
			
			break;
		}
		
		return null;
	}
	
	private int fetchInt(int ip){
		int b1 = instructions[ip] & 0xFF;
		int b2 = instructions[ip + 1] & 0xFF;
		int b3 = instructions[ip + 2] & 0xFF;
		int b4 = instructions[ip + 3] & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}
	
	private byte fetchByte(int ip){
		return instructions[ip];
	}
	
}