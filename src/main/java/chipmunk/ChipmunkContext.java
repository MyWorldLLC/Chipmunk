package chipmunk;

import static chipmunk.Opcodes.ADD;
import static chipmunk.Opcodes.AND;
import static chipmunk.Opcodes.AS;
import static chipmunk.Opcodes.BAND;
import static chipmunk.Opcodes.BNEG;
import static chipmunk.Opcodes.BOR;
import static chipmunk.Opcodes.BXOR;
import static chipmunk.Opcodes.CALL;
import static chipmunk.Opcodes.DEC;
import static chipmunk.Opcodes.DIV;
import static chipmunk.Opcodes.DUP;
import static chipmunk.Opcodes.EQ;
import static chipmunk.Opcodes.FDIV;
import static chipmunk.Opcodes.GE;
import static chipmunk.Opcodes.GETAT;
import static chipmunk.Opcodes.GETATTR;
import static chipmunk.Opcodes.GETLOCAL;
import static chipmunk.Opcodes.GOTO;
import static chipmunk.Opcodes.GT;
import static chipmunk.Opcodes.IF;
import static chipmunk.Opcodes.INC;
import static chipmunk.Opcodes.IS;
import static chipmunk.Opcodes.LE;
import static chipmunk.Opcodes.LSHIFT;
import static chipmunk.Opcodes.LT;
import static chipmunk.Opcodes.MOD;
import static chipmunk.Opcodes.MUL;
import static chipmunk.Opcodes.NEG;
import static chipmunk.Opcodes.NEW;
import static chipmunk.Opcodes.NOT;
import static chipmunk.Opcodes.OR;
import static chipmunk.Opcodes.POP;
import static chipmunk.Opcodes.POS;
import static chipmunk.Opcodes.POW;
import static chipmunk.Opcodes.PUSH;
import static chipmunk.Opcodes.RETURN;
import static chipmunk.Opcodes.RSHIFT;
import static chipmunk.Opcodes.SETAT;
import static chipmunk.Opcodes.SETATTR;
import static chipmunk.Opcodes.SETLOCAL;
import static chipmunk.Opcodes.SUB;
import static chipmunk.Opcodes.SWAP;
import static chipmunk.Opcodes.THROW;
import static chipmunk.Opcodes.TRUTH;
import static chipmunk.Opcodes.URSHIFT;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.modules.lang.CModule;
import chipmunk.modules.reflectiveruntime.CBoolean;
import chipmunk.modules.reflectiveruntime.CInteger;
import chipmunk.reflectors.ContextOperator;
import chipmunk.reflectors.ContextReflector;
import chipmunk.reflectors.Reflector;

public class ChipmunkContext {
	
	public class CallFrame {
		public final Reflector method;
		public final int ip;
		public final Reflector[] locals;
		
		public CallFrame(Reflector method, int ip, Reflector[] locals){
			this.method = method;
			this.ip = ip;
			this.locals = locals;
		}
	}

	protected Map<String, CModule> modules;
	protected List<Reflector> stack;
	protected Deque<CallFrame> frozenCallStack;
	public volatile boolean interrupted;
	private volatile boolean resuming;
	private int memHigh;
	
	private final ContextReflector trueValue;
	private final ContextReflector falseValue;
	
	public ChipmunkContext(){
		modules = new HashMap<String, CModule>();
		// initialize operand stack to be 128 elements deep
		stack = new ArrayList<Reflector>(128);
		
		frozenCallStack = new ArrayDeque<CallFrame>(128);
		memHigh = 0;
		
		trueValue = new ContextReflector(new CBoolean(true));
		falseValue = new ContextReflector(new CBoolean(false));
	}
	
	public CModule getModule(String name){
		return modules.get(name);
	}
	
	public CModule resolveModule(String name){
		// TODO - resolve module name, loading it if needed
		return null;
	}
	
	public void addModule(CModule module){
		modules.put(module.getName(), module);
	}
	
	public boolean removeModule(CModule module){
		
		CModule removed = modules.remove(module.getName());
		
		if(removed == null){
			return false;
		}else{
			return true;
		}
		
	}
	
	public void push(Reflector obj){
		if(obj == null){
			throw new NullPointerException();
		}
		stack.add(obj);
	}
	
	public Reflector pop(){
		return stack.remove(stack.size() - 1);
	}
	
	public Reflector peek(){
		return stack.get(stack.size() - 1);
	}
	
	public void dup(int index){
		Reflector obj = stack.get(stack.size() - (index + 1));
		stack.add(obj);
	}
	
	public void swap(int index1, int index2){
		int stackIndex1 = stack.size() - (index1 + 1);
		int stackIndex2 = stack.size() - (index2 + 1);
		
		Reflector obj1 = stack.get(stackIndex1);
		Reflector obj2 = stack.get(stackIndex2);
		
		stack.set(index1, obj2);
		stack.set(index2, obj1);
	}
	
	public void freeze(Reflector method, int ip, Reflector[] locals){
		frozenCallStack.push(new CallFrame(method, ip, locals));
	}
	
	public CallFrame unfreezeNext(){
		return frozenCallStack.pop();
	}
	
	public void resume(){
		// TODO - if frozen call stack isn't empty,
		// get method at top and call it.
	}
	
	public void traceMem(int newlyAllocated){
		memHigh += newlyAllocated;
	}
	
	public Reflector dispatch(byte[] instructions, int paramCount, int localCount, List<Object> constantPool){
		int ip = 0;
		Reflector[] locals;
		
		if(resuming){
			ChipmunkContext.CallFrame frame = this.unfreezeNext();
			ip = frame.ip;
			locals = frame.locals;
			
			// call into the next method to resume call stack
			try {
				 this.push(frame.method.doOp(this, "call", 0));
			} catch (SuspendedChipmunk e) {
				 this.freeze(frame.method, ip, locals);
			} catch (AngryChipmunk e) {
				// TODO - fill in stack trace or jump to exception handler
			}
		}else{
			locals = new Reflector[localCount];
			// pop arguments right->left
			// TODO - handle references to this (binding vs passing)
			for(int i = paramCount - 1; i >= 0; i++){
				locals[i] = this.pop();
			}
		}
		
		while(true){
			
			byte op = instructions[ip];
			
			Reflector rh;
			Reflector lh;
			Reflector ins;
			
			switch(op){
			
			case ADD:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "plus", rh));
				ip++;
				break;
			case SUB:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "minus", rh));
				ip++;
				break;
			case MUL:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "mul", rh));
				ip++;
				break;
			case DIV:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "div", rh));
				ip++;
				break;
			case FDIV:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "fdiv", rh));
				ip++;
				break;
			case MOD:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "mod", rh));
				ip++;
				break;
			case POW:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "pow", rh));
				ip++;
				break;
			case INC:
				lh = this.pop();
				this.push(lh.doOp(this, "inc"));
				ip++;
				break;
			case DEC:
				lh = this.pop();
				this.push(lh.doOp(this, "dec"));
				ip++;
				break;
			case POS:
				lh = this.pop();
				this.push(lh.doOp(this, "pos"));
				ip++;
				break;
			case NEG:
				lh = this.pop();
				this.push(lh.doOp(this, "neg"));
				ip++;
				break;
			case AND:
				rh = this.pop();
				lh = this.pop();
				// TODO - catch cast exception
				if(((CBoolean)lh.doOp(this, "truth").getObject()).getValue()){
					if(((CBoolean)rh.doOp(this, "truth").getObject()).getValue()){
						this.push(trueValue);
					}else{
						this.push(falseValue);
					}
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case OR:
				rh = this.pop();
				lh = this.pop();
				if(((CBoolean)lh.doOp(this, "truth").getObject()).getValue() 
						|| ((CBoolean)rh.doOp(this, "truth").getObject()).getValue()){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case BXOR:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "bxor", rh));
				ip++;
				break;
			case BAND:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "band", rh));
				ip++;
				break;
			case BOR:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "bor", rh));
				ip++;
				break;
			case BNEG:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "bneg"));
				ip++;
				break;
			case LSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "lshift", rh));
				ip++;
				break;
			case RSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "rshift", rh));
				ip++;
				break;
			case URSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "urshift", rh));
				ip++;
				break;
			case SETATTR:
				rh = this.pop();
				lh = this.pop();
				ins = this.pop();
				ins.doOp(this, "setAttr", lh, rh);
				this.push(ins);
				ip++;
				break;
			case GETATTR:
				lh = this.pop();
				ins = this.pop();
				this.push(ins.doOp(this, "getAttr", lh));
				ip++;
				break;
			case GETAT:
				lh = this.pop();
				ins = this.pop();
				this.push(ins.doOp(this, "getAt", lh));
				ip++;
				break;
			case SETAT:
				rh = this.pop();
				lh = this.pop();
				ins = this.pop();
				this.push(ins.doOp(this, "setAt", lh, rh));
				ip++;
				break;
			case GETLOCAL:
				this.push(locals[fetchByte(instructions, ip + 1)]);
				ip += 2;
				break;
			case SETLOCAL:
				locals[fetchByte(instructions, ip + 1)] = this.peek();
				ip += 2;
				break;
			case TRUTH:
				rh = this.pop();
				this.push(rh.doOp(this, "truth"));
				ip++;
				break;
			case NOT:
				rh = this.pop();
				this.push(new ContextReflector(new CBoolean(!((CBoolean)rh.doOp(this, "truth").getObject()).booleanValue())));
				ip++;
				break;
			case AS:
				lh = this.pop();
				ins = this.pop();
				this.push(ins.doOp(this, "as", lh));
				ip++;
				break;
			case NEW:
				ins = this.pop();
				this.push(ins.doOp(this, "newInstance", fetchInt(instructions, ip + 1)));
				break;
			case IF:
				ins = this.pop();
				int target = fetchInt(instructions, ip + 1);
				ip += 5;
				// TODO - catch cast exception
				if(!((CBoolean)ins.doOp(this, "truth").getObject()).booleanValue()){
					ip = target;
				}
				break;
			case CALL:
				ins = this.pop();
				// Need to bump ip BEFORE calling next method. Otherwise,
				// if suspended the ip will be stored in its old state
				// and when this method resumes after being suspended,
				// it will try to re-run this call.
				ip += 5;
				try{
					// TODO
					this.push(ins.doOp(this, "call", fetchInt(instructions, ip + 1)));
				}catch(SuspendedChipmunk e){
					this.freeze(ins, ip, locals);
				}catch(AngryChipmunk e){
					// TODO - fill in stack trace or jump to exception handler
				}
				
				break;
			case GOTO:
				int gotoIndex = fetchInt(instructions, ip + 1);
				ip = gotoIndex;
				break;
			case THROW:
				ins = this.pop();
				throw new ExceptionChipmunk(ins.getObject());
			case RETURN:
				ins = this.pop();
				return ins;
			case POP:
				ins = this.pop();
				ip++;
				break;
			case DUP:
				int dupIndex = fetchInt(instructions, ip + 1);
				this.dup(dupIndex);
				ip += 5;
				break;
			case SWAP:
				int swapIndex1 = fetchInt(instructions, ip + 1);
				int swapIndex2 = fetchInt(instructions, ip + 5);
				this.swap(swapIndex1, swapIndex2);
				ip += 9;
				break;
			case PUSH:
				int constIndex = fetchInt(instructions, ip + 1);
				Object constant = constantPool.get(constIndex);
				if(constant instanceof ContextOperator){
					this.push(new ContextReflector((ContextOperator)constant));
				}else{
					this.push(new Reflector(constant));
				}
				ip += 5;
				break;
			case EQ:
				rh = this.pop();
				lh = this.pop();
				this.push(lh.doOp(this, "equals", rh));
				ip++;
				break;
			case GT:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)lh.doOp(this, "compare", rh).getObject()).getValue() > 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case LT:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)lh.doOp(this, "compare", rh).getObject()).getValue() < 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case GE:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)lh.doOp(this, "compare", rh).getObject()).getValue() >= 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case LE:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)lh.doOp(this, "compare", rh).getObject()).getValue() <= 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case IS:
				rh = this.pop();
				lh = this.pop();
				if(lh.getObject() == rh.getObject()){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			default:
				throw new InvalidOpcodeChipmunk(op);
			}
		}
	}
	
	private int fetchInt(byte[] instructions, int ip){
		int b1 = instructions[ip] & 0xFF;
		int b2 = instructions[ip + 1] & 0xFF;
		int b3 = instructions[ip + 2] & 0xFF;
		int b4 = instructions[ip + 3] & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}
	
	private byte fetchByte(byte[] instructions, int ip){
		return instructions[ip];
	}
	
}
