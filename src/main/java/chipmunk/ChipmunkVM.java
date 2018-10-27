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

import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chipmunk.invoke.Call;
import chipmunk.invoke.CallEight;
import chipmunk.invoke.CallEightVoid;
import chipmunk.invoke.CallFive;
import chipmunk.invoke.CallFiveVoid;
import chipmunk.invoke.CallFour;
import chipmunk.invoke.CallFourVoid;
import chipmunk.invoke.CallNine;
import chipmunk.invoke.CallNineVoid;
import chipmunk.invoke.CallOne;
import chipmunk.invoke.CallOneVoid;
import chipmunk.invoke.CallSeven;
import chipmunk.invoke.CallSevenVoid;
import chipmunk.invoke.CallSix;
import chipmunk.invoke.CallSixVoid;
import chipmunk.invoke.CallTen;
import chipmunk.invoke.CallTenVoid;
import chipmunk.invoke.CallThree;
import chipmunk.invoke.CallThreeVoid;
import chipmunk.invoke.CallTwo;
import chipmunk.invoke.CallTwoVoid;
import chipmunk.invoke.CallVoid;
import chipmunk.invoke.VoidMarker;
import chipmunk.modules.runtime.CBoolean;
import chipmunk.modules.runtime.CFloat;
import chipmunk.modules.runtime.CInteger;
import chipmunk.modules.runtime.CIterator;
import chipmunk.modules.runtime.CList;
import chipmunk.modules.runtime.CMap;
import chipmunk.modules.runtime.CMethod;
import chipmunk.modules.runtime.CModule;
import chipmunk.modules.runtime.CNull;
import chipmunk.modules.runtime.CallInterceptor;
import chipmunk.modules.runtime.Initializable;
import chipmunk.modules.runtime.RuntimeObject;

public class ChipmunkVM {

	public class CallFrame {
		public final CMethod method;
		public final int ip;
		public final Object[] locals;

		public CallFrame(CMethod method, int ip, Object[] locals) {
			this.method = method;
			this.ip = ip;
			this.locals = locals;
		}
	}
	
	public class QueuedInvocation {
		
		public QueuedInvocation(CMethod method, Object[] params, ChipmunkScript state){
			this.method = method;
			this.params = params;
			this.state = state;
		}
		
		public CMethod method;
		public Object[] params;
		public ChipmunkScript state;
	}
	
	private class CallRecord {
		public Class<?>[] callTypes;
		public MethodHandle method;
	}

	private class CallArrays {
		private int paramIndex;
		private Object[] params;
		private Class<?>[] paramTypes;

		public CallArrays() {
			params = new Object[0];
			paramTypes = new Class<?>[0];
		}

		public Object[] getParams() {
			return params;
		}

		public Class<?>[] getParamTypes() {
			return paramTypes;
		}

		public void startParamFill(Object param, int length) {

			if (params == null || params.length != length) {
				params = new Object[length];
				paramTypes = new Class<?>[length];
			}

			params[0] = param;
			paramTypes[0] = param.getClass();
			paramIndex = 1;
		}

		public void addParam(Object param) {
			params[paramIndex] = param;
			paramTypes[paramIndex] = param.getClass();
			paramIndex++;
		}
	}

	private enum InternalOp {
		ADD("plus"), SUB("minus"), MUL("mul"), DIV("div"), FDIV("fdiv"), MOD("mod"), POW("pow"), INC("inc"), DEC(
				"dec"), POS("pos"), NEG("neg"), TRUTH("truth"), BXOR("bxor"), BAND("band"), BOR(
						"bor"), BNEG("bneg"), LSHIFT("lshift"), RSHIFT("rshift"), URSHIFT("urshift"), SETATTR(
								"setAttr"), GETATTR("getAttr"), SETAT("setAt"), GETAT("getAt"), AS("as"), NEWINSTANCE(
										"newInstance"), CALL("call"), EQUALS("equals"), COMPARE("compare"), INSTANCEOF(
												"instanceOf"), ITERATOR("iterator"), RANGE("range"), NEXT("next");

		private final String opName;

		private InternalOp(String op) {
			opName = op;
		}

		public String getOpName() {
			return opName;
		}
	}
	
	protected List<ModuleLoader> loaders;
	
	protected ChipmunkScript activeScript;
	
	protected Map<String, CModule> modules;
	protected Object[] stack;
	private int stackIndex;
	protected Deque<CallFrame> frozenCallStack;
	protected Deque<CModule> initializationQueue;
	
	public volatile boolean interrupted;
	private volatile boolean resuming;
	
	private int memHigh;

	private final CBoolean trueValue;
	private final CBoolean falseValue;
	
	private final int refLength;
	
	protected Map<Class<?>, Object[]> internalCallCache;
	protected Object[][] internalParams;
	protected Class<?>[][] internalTypes;
	protected Class<?>[] callTypes;
	protected Class<?>[] voidCallTypes;
	protected final MethodHandles.Lookup methodLookup;

	public ChipmunkVM() {
		internalCallCache = new HashMap<Class<?>, Object[]>();

		internalParams = new Object[5][];
		internalParams[0] = new Object[0];
		internalParams[1] = new Object[1];
		internalParams[2] = new Object[2];
		internalParams[3] = new Object[3];
		internalParams[4] = new Object[4];

		internalTypes = new Class<?>[5][];
		internalTypes[0] = new Class<?>[0];
		internalTypes[1] = new Class<?>[1];
		internalTypes[2] = new Class<?>[2];
		internalTypes[3] = new Class<?>[3];
		internalTypes[4] = new Class<?>[4];
		
		callTypes = new Class<?>[11];
		callTypes[0] = Call.class;
		callTypes[1] = CallOne.class;
		callTypes[2] = CallTwo.class;
		callTypes[3] = CallThree.class;
		callTypes[4] = CallFour.class;
		callTypes[5] = CallFive.class;
		callTypes[6] = CallSix.class;
		callTypes[7] = CallSeven.class;
		callTypes[8] = CallEight.class;
		callTypes[9] = CallNine.class;
		callTypes[10] = CallTen.class;
		
		voidCallTypes = new Class<?>[11];
		voidCallTypes[0] = CallVoid.class;
		voidCallTypes[1] = CallOneVoid.class;
		voidCallTypes[2] = CallTwoVoid.class;
		voidCallTypes[3] = CallThreeVoid.class;
		voidCallTypes[4] = CallFourVoid.class;
		voidCallTypes[5] = CallFiveVoid.class;
		voidCallTypes[6] = CallSixVoid.class;
		voidCallTypes[7] = CallSevenVoid.class;
		voidCallTypes[8] = CallEightVoid.class;
		voidCallTypes[9] = CallNineVoid.class;
		voidCallTypes[10] = CallTenVoid.class;
		

		loaders = new ArrayList<ModuleLoader>();
		
		activeScript = new ChipmunkScript(128);
		stack = new Object[128]; //activeScript.stack;
		stackIndex = 0;
		frozenCallStack = activeScript.frozenCallStack;
		
		initializationQueue = new ArrayDeque<CModule>();
		
		memHigh = 0;

		trueValue = new CBoolean(true);
		falseValue = new CBoolean(false);
		
		refLength = 8; // assume 64-bit references
		
		methodLookup = MethodHandles.lookup();
	}
	
	public void setLoaders(List<ModuleLoader> loaders){
		
		if(loaders == null){
			throw new NullPointerException("Loaders cannot be null");
		}
		
		this.loaders = loaders;
	}
	
	public List<ModuleLoader> getLoaders(){
		return loaders;
	}
	
	public void loadModule(String moduleName) throws ModuleLoadChipmunk {
		
		if(modules.containsKey(moduleName)){
			// this module is already loaded - skip
			return;
		}
		
		for(ModuleLoader loader : loaders){
			CModule module = null;
			try {
				module = loader.loadModule(moduleName);
				
				// need to record the module *before* handling imports in case
				// of a circular import
				modules.put(module.getName(), module);
				if(module != null){
					
					for(CModule.Import importedModule : module.getImports()){
						loadModule(importedModule.getName());
					}
					initializationQueue.add(module);
					
					return;
				}
			}catch(Exception e){
				throw new ModuleLoadChipmunk(e);
			}
		}
		
		throw new ModuleLoadChipmunk(String.format("Module %s not found", moduleName));
	}

	public CModule getModule(String name) {
		return modules.get(name);
	}

	public CModule resolveModule(String name) throws ModuleLoadChipmunk {
		if(!modules.containsKey(name)){
			loadModule(name);
		}
		return modules.get(name);
	}

	public void pushArgs(Object[] args){
		// push arguments right -> left
		for(int i = args.length - 1; i >= 0; i--){
			this.push(args[i]);
		}
	}
	
	public void push(Object obj) {
		try {
			stack[stackIndex] = obj;
			stackIndex++;
		}catch(ArrayIndexOutOfBoundsException e) {
			stack = Arrays.copyOf(stack, stack.length + 128);
			this.push(obj);
		}
	}

	public Object pop() {
		stackIndex--;
		return stack[stackIndex];
	}

	public Object peek() {
		return stack[stackIndex - 1];
	}

	public void dup(int index) {
		stack[stackIndex] = stack[stackIndex - (index + 1)];
		stackIndex++;
	}

	public void swap(int index1, int index2) {
		int stackIndex1 = stackIndex - (index1 + 1);
		int stackIndex2 = stackIndex - (index2 + 1);

		Object obj1 = stack[stackIndex1];
		Object obj2 = stack[stackIndex2];

		stack[index1] = obj2;
		stack[index2] = obj1;
	}

	public void freeze(CMethod method, int ip, Object[] locals) {
		frozenCallStack.push(new CallFrame(method, ip, locals));
	}

	public CallFrame unfreezeNext() {
		return frozenCallStack.pop();
	}
	
	public boolean hasNextFrame(){
		return !frozenCallStack.isEmpty();
	}

	public Object run(ChipmunkScript script) throws SuspendedChipmunk, AngryChipmunk {
		
		interrupted = false;

		activeScript = script;
		
		modules = script.modules;
		stack = new Object[128]; // script.stack;
		stackIndex = 0;
		frozenCallStack = script.frozenCallStack;
		initializationQueue = script.initializationQueue;
		
		if(script.isFrozen()){
			resuming = true;
		}else{
			loadModule(script.entryModule);
		}
		
		if(!frozenCallStack.isEmpty() && !initializationQueue.isEmpty()){
			// if the frozen call stack contains anything and we're not done initializing modules,
			// continue running initializers
			// TODO
		}
		
		while(!initializationQueue.isEmpty()){
			// At this point, the module initializers of modules this
			// module depends on have run, so we can safely import the
			// symbols now.
			
			CModule module = initializationQueue.poll();
			
			for(CModule.Import moduleImport : module.getImports()){

				Namespace importedNamespace = modules.get(moduleImport.getName()).getNamespace();
				
				if(moduleImport.isImportAll()){
					
					Set<String> importedNames = importedNamespace.names();
					
					for(String name : importedNames){
						module.getNamespace().setFinal(name, importedNamespace.get(name));
					}
				}else{
					
					List<String> symbols = moduleImport.getSymbols();
					List<String> aliases = moduleImport.getAliases();
					
					for(int i = 0; i < symbols.size(); i++){
						
						if(moduleImport.isAliased()){
							module.getNamespace().setFinal(aliases.get(i), importedNamespace.get(symbols.get(i)));
						}else{
							module.getNamespace().setFinal(symbols.get(i), importedNamespace.get(symbols.get(i)));
						}
					}
				}
			}
			
			if(module.hasInitializer()){
				this.dispatch(module.getInitializer(), 0);
			}
		}
		
		
		if(resuming){
			// TODO - if frozen call stack isn't empty,
			// get method at top and call it.
			return null;
		}else{
			// Starting to run entry method.
			CMethod entryMethod = (CMethod) activeScript.modules.get(
					activeScript.entryModule).getNamespace().get(activeScript.entryMethod);
			this.pushArgs(activeScript.entryArgs);
			return this.dispatch(entryMethod, activeScript.entryArgs.length);
		}
		
	}

	public void traceMem(int newlyAllocated) {
		memHigh += newlyAllocated;
	}
	
	public void untraceMem(int amount) {
		memHigh -= amount;
	}

	public void traceBoolean() {
		memHigh += 1;
	}
	
	public CBoolean traceBoolean(boolean value) {
		traceBoolean();
		return new CBoolean(value);
	}
	
	public void untraceBoolean() {
		memHigh -= 1;
	}

	public void traceInteger() {
		memHigh += 4;
	}
	
	public CInteger traceInteger(int value) {
		traceInteger();
		return new CInteger(value);
	}
	
	public void untraceInteger() {
		memHigh -= 4;
	}

	public void traceFloat() {
		memHigh += 4;
	}
	
	public CFloat traceFloat(float value) {
		traceFloat();
		return new CFloat(value);
	}
	
	public void untraceFloat() {
		memHigh -= 4;
	}

	public String traceString(String str) {
		memHigh += str.length() * 2;
		return str;
	}
	
	public void traceReference() {
		memHigh += refLength;
	}
	
	public Object traceReference(Object obj) {
		traceReference();
		return obj;
	}
	
	public void untraceReference() {
		memHigh -= refLength;
	}
	
	public void untraceReferences(int count) {
		memHigh -= refLength * count;
	}

	public Object dispatch(CMethod method, int paramCount) {
		int ip = 0;
		Object[] locals;

		final byte[] instructions = method.getCode();
		final Object[] callCache = method.getCallCache();
		final int localCount = method.getLocalCount();
		final Object[] constantPool = method.getConstantPool();

		if (resuming) {
			CallFrame frame = unfreezeNext();
			ip = frame.ip;
			locals = frame.locals;

			// call into the next method to resume call stack
			try {
				this.push(doInternal(InternalOp.CALL, frame.method, 0, callCache, ip));
			} catch (SuspendedChipmunk e) {
				this.freeze(frame.method, ip, locals);
			} catch (AngryChipmunk e) {
				// TODO - fill in stack trace or jump to exception handler
			}
		} else {
			locals = new Object[localCount];
			// pop arguments right->left
			for (int i = paramCount; i >= 1; i--) {
				locals[i] = this.pop();
			}
			locals[0] = method.getSelf();
		}

		while (true) {

			try {

				byte op = instructions[ip];

				Object rh;
				Object lh;
				Object ins;

				boolean cond1;
				boolean cond2;

				switch (op) {

				case ADD:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.ADD, lh, 2, callCache, ip));
					ip++;
					break;
				case SUB:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.SUB, lh, 2, callCache, ip));
					ip++;
					break;
				case MUL:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.MUL, lh, 2, callCache, ip));
					ip++;
					break;
				case DIV:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.DIV, lh, 2, callCache, ip));
					ip++;
					break;
				case FDIV:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.FDIV, lh, 2, callCache, ip));
					ip++;
					break;
				case MOD:
					rh = this.pop();
					lh = this.pop();

					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.MOD, lh, 2, callCache, ip));
					ip++;
					break;
				case POW:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.POW, lh, 2, callCache, ip));
					ip++;
					break;
				case INC:
					lh = this.pop();
					this.push(doInternal(InternalOp.INC, lh, 1, callCache, ip));
					ip++;
					break;
				case DEC:
					lh = this.pop();
					this.push(doInternal(InternalOp.DEC, lh, 1, callCache, ip));
					ip++;
					break;
				case POS:
					lh = this.pop();
					this.push(doInternal(InternalOp.POS, lh, 1, callCache, ip));
					ip++;
					break;
				case NEG:
					lh = this.pop();
					this.push(doInternal(InternalOp.NEG, lh, 1, callCache, ip));
					ip++;
					break;
				case AND:
					rh = this.pop();
					lh = this.pop();
					cond1 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					cond2 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					if (cond1 && cond2) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case OR:
					rh = this.pop();
					lh = this.pop();
					cond1 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					cond2 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					if (cond1 || cond2) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case BXOR:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.BXOR, lh, 2, callCache, ip));
					ip++;
					break;
				case BAND:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.BAND, lh, 2, callCache, ip));
					ip++;
					break;
				case BOR:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.BOR, lh, 2, callCache, ip));
					ip++;
					break;
				case BNEG:
					lh = this.pop();
					this.push(doInternal(InternalOp.BNEG, lh, 1, callCache, ip));
					ip++;
					break;
				case LSHIFT:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.LSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case RSHIFT:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.RSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case URSHIFT:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.URSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case SETATTR:
					ins = this.pop();
					lh = this.pop();
					rh = this.pop();
					internalParams[3][1] = lh;
					internalParams[3][2] = rh;
					doInternal(InternalOp.SETATTR, ins, 3, callCache, ip);
					this.push(ins);
					ip++;
					break;
				case GETATTR:
					ins = this.pop();
					lh = this.pop();
					internalParams[2][1] = lh;
					this.push(doInternal(InternalOp.GETATTR, ins, 2, callCache, ip));
					ip++;
					break;
				case GETAT:
					lh = this.pop();
					ins = this.pop();
					internalParams[2][1] = lh;
					this.push(doInternal(InternalOp.GETAT, ins, 2, callCache, ip));
					ip++;
					break;
				case SETAT:
					ins = this.pop();
					lh = this.pop();
					rh = this.pop();
					internalParams[3][1] = lh;
					internalParams[3][2] = rh;
					this.push(doInternal(InternalOp.SETAT, ins, 3, callCache, ip));
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
					this.push(doInternal(InternalOp.TRUTH, rh, 1, callCache, ip));
					ip++;
					break;
				case NOT:
					rh = this.pop();
					this.push(new CBoolean(!((CBoolean) doInternal(InternalOp.TRUTH, rh, 1, callCache, ip)).booleanValue()));
					ip++;
					break;
				case AS:
					lh = this.pop();
					ins = this.pop();
					internalParams[2][1] = lh;
					this.push(doInternal(InternalOp.AS, ins, 2, callCache, ip));
					ip++;
					break;
				case IF:
					ins = this.pop();
					int target = fetchInt(instructions, ip + 1);
					ip += 5;

					if (!((CBoolean) doInternal(InternalOp.TRUTH, ins, 1, callCache, ip)).booleanValue()) {
						ip = target;
					}
					break;
				case CALL:
					ins = this.pop();

					try {
						// TODO - probably should use the same mechanism used by
						// CALLAT here, since this
						// isn't really quite an internal operation

						internalParams[2][1] = fetchByte(instructions, ip + 1);
						this.push(doInternal(InternalOp.CALL, ins, 2, callCache, ip));
					} catch (SuspendedChipmunk e) {
						// Need to bump ip BEFORE calling next method.
						// Otherwise,
						// the ip will be stored in its old state and when this
						// method resumes after being suspended, it will try to
						// re-run this call. Skip frames with a call into a non-CMethod
						// instance.
						ip += 2;
						if(ins instanceof CMethod){
							this.freeze((CMethod)ins, ip, locals);
						}
						throw e;
					}
					ip += 2;
					break;
				case CALLAT:
					ins = this.pop();

					try {
						String methodName = (String) constantPool[fetchInt(instructions, ip + 2)];

						// TODO - this is not an internal operation, so we need
						// a different caching mechanism
						// here
						Object result = callExternal(ins, methodName, fetchByte(instructions, ip + 1));
						this.push(result != null ? result : CNull.instance());
					} catch (SuspendedChipmunk e) {
						// Need to bump ip BEFORE calling next method.
						// Otherwise,
						// the ip will be stored in its old state and when this
						// method resumes after being suspended, it will try to
						// re-run this call. Skip frames with a call into a non-CMethod
						// instance.
						ip += 6;
						if(ins instanceof CMethod){
							this.freeze((CMethod)ins, ip, locals);
						}
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
					Object constant = constantPool[constIndex];
					this.push(constant);
					ip += 5;
					break;
				case EQ:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.EQUALS, lh, 2, callCache, ip));
					ip++;
					break;
				case GT:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() > 0) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case LT:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() < 0) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case GE:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() >= 0) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case LE:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() <= 0) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case IS:
					rh = this.pop();
					lh = this.pop();
					if (lh == rh) {
						this.push(trueValue);
					} else {
						this.push(falseValue);
					}
					ip++;
					break;
				case INSTANCEOF:
					rh = this.pop();
					lh = this.pop();
					internalParams[2][1] = rh;
					this.push(doInternal(InternalOp.INSTANCEOF, lh, 2, callCache, ip));
					ip++;
					break;
				case ITER:
					ins = this.pop();
					this.push(doInternal(InternalOp.ITERATOR, ins, 1, callCache, ip));
					ip++;
					break;
				case NEXT:
					ins = this.peek();
					if (!((CIterator) ins).hasNext(this)) {
						// pop the iterator
						this.pop();
						ip = fetchInt(instructions, ip + 1);
					} else {

						this.push(doInternal(InternalOp.NEXT, ins, 1, callCache, ip));
						ip += 5;
					}
					break;
				case RANGE:
					rh = this.pop();
					lh = this.pop();
					internalParams[3][1] = rh;
					internalParams[3][2] = fetchByte(instructions, ip + 1) == 0 ? false : true;
					this.push(doInternal(InternalOp.RANGE, lh, 3, callCache, ip));
					ip += 2;
					break;
				case LIST:
					int elementCount = fetchInt(instructions, ip + 1);
					CList list = new CList();
					for(int i = 0; i < elementCount; i++){
						list.add(this, this.pop());
					}
					// elements are popped in reverse order
					list.reverse();
					traceMem(8);
					this.push(list);
					ip += 5;
					break;
				case MAP:
					elementCount = fetchInt(instructions, ip + 1);
					CMap map = new CMap();
					for(int i = 0; i < elementCount; i++){
						Object value = this.pop();
						Object key = this.pop();
						map.put(this, key, value);
					}
					traceMem(8);
					this.push(map);
					ip += 5;
					break;
				case INIT:
					ins = this.pop();
					this.push(((Initializable) ins).getInitializer());
					ip++;
					break;
				case GETMODULE:
					this.push(method.getModule().getNamespace()
							.get((String)constantPool[fetchInt(instructions, ip + 1)]));
					ip += 5;
					break;
				case SETMODULE:
					ins = this.peek();
					method.getModule()
					.getNamespace()
					.set(constantPool[
							fetchInt(instructions, ip + 1)].toString(),
							ins);
					ip += 5;
					break;
				default:
					throw new InvalidOpcodeChipmunk(op);
				}

			} catch (RuntimeException e) {
				
				// SuspendedChipmunk must be re-thrown - don't
				// allow exception handlers to run or they will
				// block suspension
				if (e instanceof SuspendedChipmunk) {
					throw e;
				}

				if (!(e instanceof AngryChipmunk)) {
					e = new AngryChipmunk(String.format("Error at ip: %d", ip), e);
				}
				// TODO - jump to exception handler (if present)
				// or fill in trace info and propagate
				throw e;
			}
		}
	}

	private int fetchInt(byte[] instructions, int ip) {
		int b1 = instructions[ip] & 0xFF;
		int b2 = instructions[ip + 1] & 0xFF;
		int b3 = instructions[ip + 2] & 0xFF;
		int b4 = instructions[ip + 3] & 0xFF;
		return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
	}

	private byte fetchByte(byte[] instructions, int ip) {
		return instructions[ip];
	}

	public Object lookupMethod(Object target, String opName, Class<?>[] callTypes) throws NoSuchMethodException {

		Method[] methods = target.getClass().getMethods();
		Method method = null;
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getName().equals(opName)) {
				// only call public methods
				if (paramTypesMatch(method.getParameterTypes(), callTypes)
						&& ((method.getModifiers() & Modifier.PUBLIC) != 0)) {
					break;
				}
			}
		}
		
		if(method == null) {
			throw new NoSuchMethodException(formatMissingMethodMessage(target.getClass(), opName, callTypes));
		}
		
		Class<?> callTypeClass = null;
		if(callTypes.length < 11) {
			// direct-bind method
			
			if(method.getReturnType().equals(void.class)) {
				callTypeClass = this.voidCallTypes[callTypes.length];
			}else {
				callTypeClass = this.callTypes[callTypes.length];
			}
			
			try {
				MethodHandle implementationHandle = methodLookup.unreflect(method);
				
				MethodType interfaceType = MethodType.methodType(callTypeClass);
				MethodType implType = MethodType.methodType(method.getReturnType(), target.getClass()).appendParameterTypes(callTypes);
				
				return LambdaMetafactory.metafactory(
						methodLookup,
						"call",
						interfaceType,
						implType.erase(),
						implementationHandle,
						implementationHandle.type())
						.getTarget().invoke();
			} catch (IllegalAccessException | LambdaConversionException e) {
				throw new AngryChipmunk(e);
			}catch(Throwable e) {
				throw new AngryChipmunk(e);
			}
			
		}else {
			// non-statically bind method
			try {
				return methodLookup.unreflect(method).asSpreader(1, Object[].class, callTypes.length);
			} catch (IllegalAccessException e) {
				throw new NoSuchMethodException(formatMissingMethodMessage(target.getClass(), opName, callTypes));
			}
		}
	}

	private boolean paramTypesMatch(Class<?>[] targetTypes, Class<?>[] callTypes) {

		if (targetTypes.length != callTypes.length) {
			return false;
		}

		for (int i = 0; i < targetTypes.length; i++) {
			if (targetTypes[i] != callTypes[i]) {
				if (!targetTypes[i].isAssignableFrom(callTypes[i])) {
					return false;
				}
			}
		}

		return true;
	}

	private Object doInternal(InternalOp op, Object target, int paramCount, Object[] callCache, int callCacheIndex) {
		Class<?> targetType = target.getClass();

		Object[] params = internalParams[paramCount];
		if(paramCount > 0) {
			params[0] = this;
		}
		
		try {

			Object method = getOrCacheInternal(op, target, params, callCache, callCacheIndex);
			Object retVal = invoke(method, target, params);
		
			// the following is helpful when weird bugs crop up - it nulls the parameter array after use
			// Arrays.fill(params, null);
			return retVal != null ? retVal : CNull.instance();
		} catch (IllegalStateException | ClassCastException | WrongMethodTypeException e) {
			// rebind the cached method and attempt to invoke again with the actual parameters we have now
			try {
				Class<?>[] paramTypes = internalTypes[paramCount];
				for (int i = 0; i < paramCount; i++) {
					paramTypes[i] = params[i].getClass();
				}
				Object method = lookupMethod(target, op.getOpName(), paramTypes);
				cacheInternal(op, targetType, method);
				
				method = getOrCacheInternal(op, target, params, callCache, callCacheIndex);
				Object retVal = invoke(method, target, params);
				// the following is helpful when weird bugs crop up - it nulls the parameter array after use
				// Arrays.fill(params, null);
				return retVal != null ? retVal : CNull.instance();
			}catch(NoSuchMethodException ex) {
				throw new AngryChipmunk(ex);
			}catch(Throwable ex) {
				throw new AngryChipmunk(ex);
			}
			
		} catch (Throwable e) {
			throw new AngryChipmunk(e);
		}
		
	}

	private Object callExternal(Object target, String methodName, byte paramCount) {

		if(target instanceof CallInterceptor){
			Object result = ((CallInterceptor) target).callAt(this, methodName, paramCount);
			// null result indicates that the interceptor did not intercept the call, so
			// continue with a plain dispatch
			if(result != null){
				return result;
			}
		}
		
		Object[] params = null;

		if (target instanceof RuntimeObject) {
			params = new Object[paramCount + 1];

			// pop arguments right->left
			for (int i = paramCount; i >= 1; i--) {
				params[i] = this.pop();
			}
			params[0] = this;
		} else {
			params = new Object[paramCount];

			// pop arguments right->left
			for (int i = paramCount - 1; i >= 0; i--) {
				params[i] = this.pop();
			}
		}
		
		Class<?>[] paramTypes = new Class<?>[params.length];

		for (int i = 0; i < params.length; i++) {
			paramTypes[i] = params[i].getClass();
		}

		try {
			Object invokeTarget = lookupMethod(target, methodName, paramTypes);
			Object retVal = invoke(invokeTarget, target, params);
			
			return retVal != null ? retVal : CNull.instance();
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new AngryChipmunk(e);
		} catch (Throwable e) {
			throw new AngryChipmunk(e);
		}
	}
	
	public Object invoke(Object callTarget, Object target, Object[] params) throws Throwable {

		if(callTarget instanceof VoidMarker) {
			switch (params.length) {
			case 0:
				((CallVoid) callTarget).call(target);
				break;
			case 1:
				((CallOneVoid) callTarget).call(target, params[0]);
				break;
			case 2:
				((CallTwoVoid) callTarget).call(target, params[0], params[1]);
				break;
			case 3:
				((CallThreeVoid) callTarget).call(target, params[0], params[1], params[2]);
				break;
			case 4:
				((CallFourVoid) callTarget).call(target, params[0], params[1], params[2], params[3]);
				break;
			case 5:
				((CallFiveVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4]);
				break;
			case 6:
				((CallSixVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5]);
				break;
			case 7:
				((CallSevenVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6]);
				break;
			case 8:
				((CallEightVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
				break;
			case 9:
				((CallNineVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8]);
				break;
			case 10:
				((CallTenVoid) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8], params[9]);
				break;
			default:
				// target is a non-direct method handle.
				MethodHandle handle = (MethodHandle) callTarget;
				handle.invoke(target, params);
			}
			return CNull.instance();
		}else {
			Object retVal = null;
			switch (params.length) {
			case 0:
				retVal = ((Call) callTarget).call(target);
				break;
			case 1:
				retVal = ((CallOne) callTarget).call(target, params[0]);
				break;
			case 2:
				retVal = ((CallTwo) callTarget).call(target, params[0], params[1]);
				break;
			case 3:
				retVal = ((CallThree) callTarget).call(target, params[0], params[1], params[2]);
				break;
			case 4:
				retVal = ((CallFour) callTarget).call(target, params[0], params[1], params[2], params[3]);
				break;
			case 5:
				retVal = ((CallFive) callTarget).call(target, params[0], params[1], params[2], params[3], params[4]);
				break;
			case 6:
				retVal = ((CallSix) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5]);
				break;
			case 7:
				retVal = ((CallSeven) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6]);
				break;
			case 8:
				retVal = ((CallEight) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
				break;
			case 9:
				retVal = ((CallNine) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8]);
				break;
			case 10:
				retVal = ((CallTen) callTarget).call(target, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7], params[8], params[9]);
				break;
			default:
				// target is a non-direct method handle.
				MethodHandle handle = (MethodHandle) callTarget;
				retVal = handle.invoke(target, params);
			}
			
			return retVal;
		}
	}
	
	private Object getOrCacheInternal(InternalOp op, Object target, Object[] params, Object[] callCache, int callCacheIndex) throws NoSuchMethodException {
		Class<?> targetType = target.getClass();
		
		Object method = callCache[callCacheIndex];
		
		if(method == null) {
			method = getCachedInternalOpMethod(op, targetType);
		}
		
		if (method == null) {
			Class<?>[] paramTypes = internalTypes[params.length];
			for (int i = 0; i < params.length; i++) {
				paramTypes[i] = params[i].getClass();
			}
			method = lookupMethod(target, op.getOpName(), paramTypes);
			cacheInternal(op, targetType, method);
		}
		
		callCache[callCacheIndex] = method;
		
		return method;
	}
	
	private void cacheInternal(InternalOp op, Class<?> targetType, Object methodImpl) {
		
		Object[] records = internalCallCache.get(targetType);
		if (records == null) {
			records = new Object[InternalOp.values().length];
			internalCallCache.put(targetType, records);
		}
		
		records[op.ordinal()] = methodImpl;
	}
	
	private Object getCachedInternalOpMethod(InternalOp op, Class<?> targetType) {
		Object[] records = internalCallCache.get(targetType);
		if(records == null) {
			return null;
		}
		return records[op.ordinal()];
	}

	private String formatMissingMethodMessage(Class<?> targetType, String methodName, Class<?>[] paramTypes) {
		StringBuilder sb = new StringBuilder();
		sb.append("No suitable method found: ");
		sb.append(targetType.getName());
		sb.append('.');
		sb.append(methodName);
		sb.append('(');

		for (int i = 0; i < paramTypes.length; i++) {
			sb.append(paramTypes[i].getName());
			if (i < paramTypes.length - 1) {
				sb.append(',');
			}
		}
		sb.append(')');
		return sb.toString();
	}
}
