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
import static chipmunk.Opcodes.GOTO;
import static chipmunk.Opcodes.GT;
import static chipmunk.Opcodes.IF;
import static chipmunk.Opcodes.INC;
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
import static chipmunk.Opcodes.NEW;
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
import static chipmunk.Opcodes.SUB;
import static chipmunk.Opcodes.SWAP;
import static chipmunk.Opcodes.THROW;
import static chipmunk.Opcodes.TRUTH;
import static chipmunk.Opcodes.URSHIFT;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chipmunk.modules.lang.CModule;
import chipmunk.modules.reflectiveruntime.CBoolean;
import chipmunk.modules.reflectiveruntime.CInteger;
import chipmunk.modules.reflectiveruntime.CIterator;
import chipmunk.modules.reflectiveruntime.CList;
import chipmunk.modules.reflectiveruntime.CMap;
import chipmunk.modules.reflectiveruntime.CMethod;
import chipmunk.modules.reflectiveruntime.CNull;
import chipmunk.reflectors.VMOperator;

public class ChipmunkVM {
	
	public class CallFrame {
		public final Object method;
		public final int ip;
		public final Object[] locals;
		
		public CallFrame(Object method, int ip, Object[] locals){
			this.method = method;
			this.ip = ip;
			this.locals = locals;
		}
	}
	
	private class CallRecord {
		public Class<?> targetType;
		public Class<?>[] callTypes;
		public Method method;
	}
	
	private enum InternalOp {
		ADD("plus"), SUB("minus"), MUL("mul"), DIV("div"), FDIV("fdiv"), MOD("mod"), POW("pow"), INC("inc"), DEC("dec"),
		POS("pos"), NEG("neg"), TRUTH("truth"), BXOR("bxor"), BAND("band"), BOR("bor"), BNEG("bneg"), LSHIFT("lshift"),
		RSHIFT("rshift"), URSHIFT("urshift"), SETATTR("setAttr"), GETATTR("getAttr"), SETAT("setAt"), GETAT("getAt"),
		AS("as"), NEWINSTANCE("newInstance"), CALL("call"), EQUALS("equals"), COMPARE("compare"), INSTANCEOF("instanceOf"),
		ITERATOR("iterator"), RANGE("range"), NEXT("next");
		
		private final String opName;
		private InternalOp(String op){
			opName = op;
		}
		
		public String getOpName(){
			return opName;
		}
	}
	
	protected Map<Class<?>, CallRecord[]> internalCallCache;

	protected Map<String, CModule> modules;
	protected List<Object> stack;
	protected Deque<CallFrame> frozenCallStack;
	public volatile boolean interrupted;
	private volatile boolean resuming;
	private int memHigh;
	
	private final CBoolean trueValue;
	private final CBoolean falseValue;
	
	
	public ChipmunkVM(){
		internalCallCache = new HashMap<Class<?>, CallRecord[]>();
		
		modules = new HashMap<String, CModule>();
		// initialize operand stack to be 128 elements deep
		stack = new ArrayList<Object>(128);
		
		frozenCallStack = new ArrayDeque<CallFrame>(128);
		memHigh = 0;
		
		trueValue = new CBoolean(true);
		falseValue = new CBoolean(false);
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
	
	public void push(Object obj){
		if(obj == null){
			throw new NullPointerException();
		}
		stack.add(obj);
	}
	
	public Object pop(){
		return stack.remove(stack.size() - 1);
	}
	
	public Object peek(){
		return stack.get(stack.size() - 1);
	}
	
	public void dup(int index){
		Object obj = stack.get(stack.size() - (index + 1));
		stack.add(obj);
	}
	
	public void swap(int index1, int index2){
		int stackIndex1 = stack.size() - (index1 + 1);
		int stackIndex2 = stack.size() - (index2 + 1);
		
		Object obj1 = stack.get(stackIndex1);
		Object obj2 = stack.get(stackIndex2);
		
		stack.set(index1, obj2);
		stack.set(index2, obj1);
	}
	
	public void freeze(Object method, int ip, Object[] locals){
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
	
	public void traceBoolean(){
		memHigh += 1;
	}
	
	public void traceInteger(){
		memHigh += 4;
	}
	
	public void traceFloat(){
		memHigh += 4;
	}
	
	public void traceString(String str){
		memHigh += str.length() * 2;
	}
	
	public Object dispatch(CMethod method, int paramCount){
		int ip = 0;
		Object[] locals;
		
		final byte[] instructions = method.getCode();
		final int localCount = method.getLocalCount();
		final List<Object> constantPool = method.getConstantPool();
		
		if(resuming){
			ChipmunkVM.CallFrame frame = this.unfreezeNext();
			ip = frame.ip;
			locals = frame.locals;
			
			// call into the next method to resume call stack
			try {
				 this.push(doInternal(InternalOp.CALL, frame.method));
			} catch (SuspendedChipmunk e) {
				 this.freeze(frame.method, ip, locals);
			} catch (AngryChipmunk e) {
				// TODO - fill in stack trace or jump to exception handler
			}
		}else{
			locals = new Object[localCount];
			// pop arguments right->left
			for(int i = paramCount; i >= 1; i--){
				locals[i] = this.pop();
			}
			locals[0] = method.getSelf();
		}
		
		while(true){
			
			byte op = instructions[ip];
			
			Object rh;
			Object lh;
			Object ins;
			
			switch(op){
			
			case ADD:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.ADD, lh, this, rh));
				ip++;
				break;
			case SUB:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.SUB, lh, this, rh));
				ip++;
				break;
			case MUL:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.MUL, lh, this, rh));
				ip++;
				break;
			case DIV:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.DIV, lh, this, rh));
				ip++;
				break;
			case FDIV:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.FDIV, lh, this, rh));
				ip++;
				break;
			case MOD:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.MOD, lh, this, rh));
				ip++;
				break;
			case POW:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.POW, lh, this, rh));
				ip++;
				break;
			case INC:
				lh = this.pop();
				this.push(doInternal(InternalOp.INC, lh, this));
				ip++;
				break;
			case DEC:
				lh = this.pop();
				this.push(doInternal(InternalOp.DEC, lh, this));
				ip++;
				break;
			case POS:
				lh = this.pop();
				this.push(doInternal(InternalOp.POS, lh, this));
				ip++;
				break;
			case NEG:
				lh = this.pop();
				this.push(doInternal(InternalOp.NEG, lh, this));
				ip++;
				break;
			case AND:
				rh = this.pop();
				lh = this.pop();
				// TODO - catch cast exception
				if(((CBoolean)doInternal(InternalOp.TRUTH, lh, this)).getValue()){
					if(((CBoolean)doInternal(InternalOp.TRUTH, rh, this)).getValue()){
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
				if(((CBoolean)doInternal(InternalOp.TRUTH, lh, this)).getValue() 
						|| ((CBoolean)doInternal(InternalOp.TRUTH, rh, this)).getValue()){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case BXOR:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.BXOR, lh, this, rh));
				ip++;
				break;
			case BAND:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.BAND, lh, this, rh));
				ip++;
				break;
			case BOR:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.BOR, lh, this, rh));
				ip++;
				break;
			case BNEG:
				lh = this.pop();
				this.push(doInternal(InternalOp.BNEG, lh, this));
				ip++;
				break;
			case LSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.LSHIFT, lh, this, rh));
				ip++;
				break;
			case RSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.RSHIFT, lh, this, rh));
				ip++;
				break;
			case URSHIFT:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.URSHIFT, lh, this, rh));
				ip++;
				break;
			case SETATTR:
				rh = this.pop();
				lh = this.pop();
				ins = this.pop();
				doInternal(InternalOp.SETATTR, ins, this, lh, rh);
				this.push(ins);
				ip++;
				break;
			case GETATTR:
				lh = this.pop();
				ins = this.pop();
				this.push(doInternal(InternalOp.GETATTR, ins, this, lh));
				ip++;
				break;
			case GETAT:
				lh = this.pop();
				ins = this.pop();
				this.push(doInternal(InternalOp.GETAT, ins, this, lh));
				ip++;
				break;
			case SETAT:
				rh = this.pop();
				lh = this.pop();
				ins = this.pop();
				this.push(doInternal(InternalOp.SETAT, ins, this, lh, rh));
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
				this.push(doInternal(InternalOp.TRUTH, rh, this));
				ip++;
				break;
			case NOT:
				rh = this.pop();
				this.push(new CBoolean(!((CBoolean)doInternal(InternalOp.TRUTH, rh, this)).booleanValue()));
				ip++;
				break;
			case AS:
				lh = this.pop();
				ins = this.pop();
				this.push(doInternal(InternalOp.AS, ins, this, lh));
				ip++;
				break;
			case NEW:
				ins = this.pop();
				this.push(doInternal(InternalOp.NEWINSTANCE, ins, this, fetchInt(instructions, ip + 1)));
				break;
			case IF:
				ins = this.pop();
				int target = fetchInt(instructions, ip + 1);
				ip += 5;
				// TODO - catch cast exception
				if(!((CBoolean)doInternal(InternalOp.TRUTH, ins, this)).booleanValue()){
					ip = target;
				}
				break;
			case CALL:
				ins = this.pop();
				
				try{
					// TODO - probably should use the same mechanism used by CALLAT here, since this
					// isn't really quite an internal operation
					this.push(doInternal(InternalOp.CALL, ins, this, fetchByte(instructions, ip + 1)));
				}catch(SuspendedChipmunk e){
					// Need to bump ip BEFORE calling next method. Otherwise,
					// the ip will be stored in its old state and when this 
					// method resumes after being suspended, it will try to 
					// re-run this call.
					ip += 2;
					this.freeze(ins, ip, locals);
					throw e;
				}catch(AngryChipmunk e){
					// TODO - fill in stack trace or jump to exception handler
					throw e;
				}
				ip += 2;
				break;
			case CALLAT:
				ins = this.pop();
				
				try{
					String methodName = (String) constantPool.get(fetchInt(instructions, ip + 2));
					
					// TODO - this is not an internal operation, so we need a different caching mechanism
					// here
					Object result = callExternal(ins, methodName, fetchByte(instructions, ip + 1));
					this.push(result != null ? result : new CNull());
				}catch(SuspendedChipmunk e){
					// Need to bump ip BEFORE calling next method. Otherwise,
					// the ip will be stored in its old state and when this 
					// method resumes after being suspended, it will try to 
					// re-run this call.
					ip += 6;
					this.freeze(ins, ip, locals);
					throw e;
				}catch(AngryChipmunk e){
					// TODO - fill in stack trace or jump to exception handler
					throw e;
				}
				ip += 6;
				break;
			case GOTO:
				int gotoIndex = fetchInt(instructions, ip + 1);
				ip = gotoIndex;
				break;
			case THROW:
				ins = this.pop();
				throw new ExceptionChipmunk(ins);
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
				this.push(constant);
				ip += 5;
				break;
			case EQ:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.EQUALS, lh, this, rh));
				ip++;
				break;
			case GT:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)doInternal(InternalOp.COMPARE, lh, this, rh)).getValue() > 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case LT:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)doInternal(InternalOp.COMPARE, lh, this, rh)).getValue() < 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case GE:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)doInternal(InternalOp.COMPARE, lh, this, rh)).getValue() >= 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case LE:
				rh = this.pop();
				lh = this.pop();
				if(((CInteger)doInternal(InternalOp.COMPARE, lh, this, rh)).getValue() <= 0){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case IS:
				rh = this.pop();
				lh = this.pop();
				if(lh == rh){
					this.push(trueValue);
				}else{
					this.push(falseValue);
				}
				ip++;
				break;
			case INSTANCEOF:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.INSTANCEOF, lh, this, rh));
				ip++;
				break;
			case ITER:
				ins = this.pop();
				this.push(doInternal(InternalOp.ITERATOR, ins, this));
				ip++;
				break;
			case NEXT:
				ins = this.peek();
				// TODO - catch cast exception
				if(!((CIterator)ins).hasNext(this)){
					// pop the iterator
					this.pop();
					ip = fetchInt(instructions, ip + 1);
				}else{
					this.push(doInternal(InternalOp.NEXT, ins, this));
					ip += 5;
				}
				break;
			case RANGE:
				rh = this.pop();
				lh = this.pop();
				this.push(doInternal(InternalOp.RANGE, lh, this, rh, fetchByte(instructions, ip + 1) == 0 ? false : true));
				ip += 2;
				break;
			case LIST:
				traceMem(8);
				this.push(new CList());
				ip++;
				break;
			case MAP:
				traceMem(8);
				this.push(new CMap());
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
	
	private Method lookupMethod(Object target, String opName, Class<?>[] callTypes) throws NoSuchMethodException {
		
		Method[] methods = target.getClass().getMethods();
		
		for(int i = 0; i < methods.length; i++){
			Method method = methods[i];
			if(method.getName().equals(opName)){
				// only call public methods
				if(paramTypesMatch(method.getParameterTypes(), callTypes) && ((method.getModifiers() & Modifier.PUBLIC) != 0)){
					// suppress access checks
					method.setAccessible(true);
					return method;
				}
			}
		}
		
		throw new NoSuchMethodException();
	}
	
	private boolean paramTypesMatch(Class<?>[] targetTypes, Class<?>[] callTypes){
		
		if(targetTypes.length != callTypes.length){
			return false;
		}
		
		for(int i = 0; i < targetTypes.length; i++){
			if(targetTypes[i] != callTypes[i]){
				if(!targetTypes[i].isAssignableFrom(callTypes[i])){
					return false;
				}
			}
		}
		
		return true;
	}
	
	private Object doInternal(InternalOp op, Object target, Object...params){
		Class<?> targetType = target.getClass();
		
		CallRecord[] records = internalCallCache.get(targetType);
		
		Class<?>[] paramTypes = new Class<?>[params.length];
		
		for(int i = 0; i < params.length; i++){
			paramTypes[i] = params[i].getClass();
		}
		
		if(records == null){
			records = new CallRecord[InternalOp.values().length];
			internalCallCache.put(target.getClass(), records);
		}
		
		final int opIndex = op.ordinal();
		
		CallRecord record = records[opIndex];
		
		if(record == null || !paramTypesMatch(record.callTypes, paramTypes)){
			// lookup & make call record
			try {
				Method method = lookupMethod(target, op.getOpName(), paramTypes);
				
				record = new CallRecord();
				record.method = method;
				record.targetType = target.getClass();
				record.callTypes = paramTypes;
				
				records[opIndex] = record;
			}catch(NoSuchMethodException e){
				// TODO
				e.printStackTrace();
			}
		}
		
		Method method = record.method;
		
		try {
			return method.invoke(target, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Object callExternal(Object target, String methodName, byte paramCount){
		
		Object[] params = null;
		
		if(target instanceof VMOperator){
			params = new Object[paramCount + 1];
			
			// pop arguments right->left
			for(int i = paramCount; i >= 1; i--){
				params[i] = this.pop();
			}
			params[0] = this;
		}else{
			params = new Object[paramCount];
			
			// pop arguments right->left
			for(int i = paramCount - 1; i >= 0; i--){
				params[i] = this.pop();
			}
		}
		
		Class<?>[] paramTypes = new Class<?>[params.length];
		
		for(int i = 0; i < params.length; i++){
			paramTypes[i] = params[i].getClass();
		}
		
		try {
			Method method = lookupMethod(target, methodName, paramTypes);
			return method.invoke(target, params);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
