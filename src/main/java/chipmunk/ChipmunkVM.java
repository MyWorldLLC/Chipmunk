package chipmunk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import chipmunk.compiler.ChipmunkCompiler;
import chipmunk.compiler.CompileChipmunk;
import chipmunk.compiler.SyntaxErrorChipmunk;
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
import chipmunk.modules.runtime.*;

import static chipmunk.Opcodes.*;

public class ChipmunkVM {

	public class CallFrame {
		public final CMethod method;
		public final int ip;
		public final Object[] locals;
		public final OperandStack stack;

		public CallFrame(CMethod method, int ip, Object[] locals, OperandStack stack) {
			this.method = method;
			this.ip = ip;
			this.locals = locals;
			this.stack = stack;
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
	//protected Object[] stack;
	//private int stackIndex;
	protected Deque<CallFrame> frozenCallStack;
	protected Deque<CModule> initializationQueue;
	
	public volatile boolean interrupted;
	//private volatile boolean resuming;
	
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
		
		activeScript = new ChipmunkScript(128);
		frozenCallStack = activeScript.frozenCallStack;
		
		initializationQueue = new ArrayDeque<CModule>();
		
		memHigh = 0;

		trueValue = new CBoolean(true);
		falseValue = new CBoolean(false);
		
		refLength = 8; // assume 64-bit references
		
		methodLookup = MethodHandles.lookup();
	}
	
	public List<ModuleLoader> getLoaders(){
		return activeScript.getLoaders();
	}
	
	public CModule loadModule(String moduleName) throws ModuleLoadChipmunk {
		
		if(modules.containsKey(moduleName)){
			// this module is already loaded - skip
			return modules.get(moduleName);
		}
		
		for(ModuleLoader loader : loaders){
			try {
				CModule module = loader.loadModule(moduleName);
				if(module != null){
					// need to record the module *before* handling imports in case
					// of a circular import
					modules.put(module.getName(), module);
					
					return module;
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
	
	public void freeze(CMethod method, int ip, Object[] locals, OperandStack stack) {
		frozenCallStack.push(new CallFrame(method, ip, locals, stack));
	}

	public CallFrame unfreezeNext() {
		return frozenCallStack.pop();
	}
	
	public boolean hasNextFrame(){
		return !frozenCallStack.isEmpty();
	}

	public static ChipmunkScript compile(InputStream is, String scriptName) throws SyntaxErrorChipmunk, CompileChipmunk, IOException {
		ChipmunkCompiler compiler = new ChipmunkCompiler();

		List<CModule> modules = compiler.compile(is, scriptName);

		CModule mainModule = null;
		for (CModule module : modules) {
			if (module.getNamespace().has("main")) {
				mainModule = module;
				break;
			}
		}

		if (mainModule == null) {
			throw new IllegalArgumentException("Script contains no main method");
		}

		ChipmunkScript script = new ChipmunkScript();
		script.setEntryCall(mainModule.getName(), "main");

		MemoryModuleLoader loader = new MemoryModuleLoader();
		loader.addModule(ChipmunkLangModuleBuilder.build());
		loader.addModules(modules);
		script.getLoaders().add(loader);

		return script;
	}
	
	public static Object run(InputStream is, String scriptName) throws SyntaxErrorChipmunk, CompileChipmunk, IOException {
		
		ChipmunkScript script = compile(is, scriptName);
		ChipmunkVM vm = new ChipmunkVM();
		
		return vm.run(script);
	}

	public Object run(ChipmunkScript script) throws AngryChipmunk {
		
		interrupted = false;

		activeScript = script;

		loaders = script.getLoaders();
		modules = script.getModules();
		frozenCallStack = script.frozenCallStack;

		if(!activeScript.isInitialized()){
			loadModule(script.entryModule);

			CModule entryModule = activeScript.modules.get(activeScript.entryModule);
			if(entryModule.hasInitializer()){
				// The entry module has an initializer that must run before the entry method.
				// Push a frozen call frame to invoke it.

				CMethod initializer = entryModule.getInitializer();
				Object[] initLocals = new Object[initializer.getLocalCount() + 1];
				initLocals[0] = initializer.getSelf();

				OperandStack initStack = new OperandStack();
				freeze(initializer, 0, initLocals, initStack);
			}

			CMethod entryMethod = (CMethod) entryModule.getNamespace().get(activeScript.entryMethod);

			Object[] entryLocals = new Object[entryMethod.getLocalCount() + 1];
			entryLocals[0] = entryMethod.getSelf();

			OperandStack entryStack = new OperandStack();
			entryStack.pushArgs(activeScript.entryArgs);

			freeze(entryMethod, 0, entryLocals, entryStack);


			activeScript.markInitialized();
		}

		if(hasNextFrame()){
			// If frozen call stack isn't empty,
			// continue dispatch
			return this.dispatch(frozenCallStack.peek().method, null);
		}

		return null;
	}

	public void interrupt(){
		interrupted = true;
	}

	public void forceSuspend() throws SuspendedChipmunk {
		interrupt();
		throw new SuspendedChipmunk();
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

	public void checkArity(Object[] params, int arity) throws IllegalArgumentException {
		if(params == null && arity != 0){
			throw new IllegalArgumentException("Parameter array is null");
		}

		if(params.length != arity){
			throw new IllegalArgumentException(String.format("Expected %d parameters, was passed %d", arity, params.length));
		}
	}
	
//	public void dumpOperandStack(OperandStack stack) {
//		System.out.println("Stack depth: " + stackDepth(stack));
//		for(int i = 0; i < stackDepth(stack); i++) {
//			System.out.println(stack.stack[i].toString());
//		}
//	}
//	
//	public int stackDepth(OperandStack stack) {
//		return stack.stackIndex;
//	}

	public Object dispatch(CMethod method, Object[] parameters) {
		int ip = 0;
		Object[] locals;
		OperandStack stack;
		
		final byte[] instructions = method.getCode().getCode();
		final Object[] callCache = method.getCode().getCallCache();
		final int localCount = method.getCode().getLocalCount();
		final Object[] constantPool = method.getCode().getConstantPool();

		if (frozenCallStack.size() > 0) {
			CallFrame frame = unfreezeNext();
			ip = frame.ip;
			locals = frame.locals;
			stack = frame.stack;

			// call into the next method to resume call stack
			try {
				if(frozenCallStack.size() > 0){
					stack.push(this.dispatch(frozenCallStack.peek().method, null));
				}
			} catch (SuspendedChipmunk e) {
				this.freeze(frame.method, ip, locals, stack);
				throw e;
			} catch (Exception e) {
				// handle exception - fill in trace or jump to handler
				if (!(e instanceof AngryChipmunk)) {
					e = new AngryChipmunk(e.getMessage(), e);
				}
				
				AngryChipmunk ex = (AngryChipmunk) e;
				
				CTraceFrame trace = new CTraceFrame();
				trace.setDebugSymbol(method.getDebugSymbol());
				trace.lineNumber = findLineNumber(ip, method.getCode().getDebugTable());
				
				ex.addTraceFrame(trace);
				
				ExceptionBlock handler = chooseExceptionHandler(ip, method.getCode().getExceptionTable());
				if(handler != null) {
					ip = handler.catchIndex;
					locals[handler.exceptionLocalIndex] = e;
				}else {
					throw ex;
				}
			}
		} else {
			locals = new Object[localCount + 1];
			stack = new OperandStack();
			locals[0] = method.getSelf();
			if (parameters != null) {
				// copy parameters
				for (int i = 0; i < parameters.length; i++) {
					locals[i + 1] = parameters[i];
				}
			}
		}

		while (true) {

			try {

				if(interrupted){
					// TODO - this is reading a volatile field on every operation
					throw new SuspendedChipmunk();
				}

				byte op = instructions[ip];

				Object rh;
				Object lh;
				Object ins;

				boolean cond1;
				boolean cond2;

				switch (op) {

				case ADD:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.ADD, lh, 2, callCache, ip));
					ip++;
					break;
				case SUB:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.SUB, lh, 2, callCache, ip));
					ip++;
					break;
				case MUL:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.MUL, lh, 2, callCache, ip));
					ip++;
					break;
				case DIV:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.DIV, lh, 2, callCache, ip));
					ip++;
					break;
				case FDIV:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.FDIV, lh, 2, callCache, ip));
					ip++;
					break;
				case MOD:
					rh = stack.pop();
					lh = stack.pop();

					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.MOD, lh, 2, callCache, ip));
					ip++;
					break;
				case POW:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.POW, lh, 2, callCache, ip));
					ip++;
					break;
				case INC:
					lh = stack.pop();
					stack.push(doInternal(InternalOp.INC, lh, 1, callCache, ip));
					ip++;
					break;
				case DEC:
					lh = stack.pop();
					stack.push(doInternal(InternalOp.DEC, lh, 1, callCache, ip));
					ip++;
					break;
				case POS:
					lh = stack.pop();
					stack.push(doInternal(InternalOp.POS, lh, 1, callCache, ip));
					ip++;
					break;
				case NEG:
					lh = stack.pop();
					stack.push(doInternal(InternalOp.NEG, lh, 1, callCache, ip));
					ip++;
					break;
				case AND:
					rh = stack.pop();
					lh = stack.pop();
					cond1 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					cond2 = ((CBoolean) doInternal(InternalOp.TRUTH, rh, 1, callCache, ip)).getValue();
					if (cond1 && cond2) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case OR:
					rh = stack.pop();
					lh = stack.pop();
					cond1 = ((CBoolean) doInternal(InternalOp.TRUTH, lh, 1, callCache, ip)).getValue();
					cond2 = ((CBoolean) doInternal(InternalOp.TRUTH, rh, 1, callCache, ip)).getValue();
					if (cond1 || cond2) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case BXOR:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.BXOR, lh, 2, callCache, ip));
					ip++;
					break;
				case BAND:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.BAND, lh, 2, callCache, ip));
					ip++;
					break;
				case BOR:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.BOR, lh, 2, callCache, ip));
					ip++;
					break;
				case BNEG:
					lh = stack.pop();
					stack.push(doInternal(InternalOp.BNEG, lh, 1, callCache, ip));
					ip++;
					break;
				case LSHIFT:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.LSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case RSHIFT:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.RSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case URSHIFT:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.URSHIFT, lh, 2, callCache, ip));
					ip++;
					break;
				case SETATTR:
					ins = stack.pop();
					lh = stack.pop();
					rh = stack.pop();
					internalParams[3][1] = lh;
					internalParams[3][2] = rh;
					doInternal(InternalOp.SETATTR, ins, 3, callCache, ip);
					stack.push(ins);
					ip++;
					break;
				case GETATTR:
					ins = stack.pop();
					rh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.GETATTR, ins, 2, callCache, ip));
					ip++;
					break;
				case GETAT:
					lh = stack.pop();
					ins = stack.pop();
					internalParams[2][1] = lh;
					stack.push(doInternal(InternalOp.GETAT, ins, 2, callCache, ip));
					ip++;
					break;
				case SETAT:
					ins = stack.pop();
					lh = stack.pop();
					rh = stack.pop();
					internalParams[3][1] = lh;
					internalParams[3][2] = rh;
					stack.push(doInternal(InternalOp.SETAT, ins, 3, callCache, ip));
					ip++;
					break;
				case GETLOCAL:
					stack.push(locals[fetchByte(instructions, ip + 1)]);
					ip += 2;
					break;
				case SETLOCAL:
					locals[fetchByte(instructions, ip + 1)] = stack.peek();
					ip += 2;
					break;
				case TRUTH:
					rh = stack.pop();
					stack.push(doInternal(InternalOp.TRUTH, rh, 1, callCache, ip));
					ip++;
					break;
				case NOT:
					rh = stack.pop();
					stack.push(new CBoolean(!((CBoolean) doInternal(InternalOp.TRUTH, rh, 1, callCache, ip)).booleanValue()));
					ip++;
					break;
				case AS:
					lh = stack.pop();
					ins = stack.pop();
					internalParams[2][1] = lh;
					stack.push(doInternal(InternalOp.AS, ins, 2, callCache, ip));
					ip++;
					break;
				case IF:
					ins = stack.pop();
					int target = fetchInt(instructions, ip + 1);
					ip += 5;

					if (!((CBoolean) doInternal(InternalOp.TRUTH, ins, 1, callCache, ip)).booleanValue()) {
						ip = target;
					}
					break;
				case CALL:
					ins = stack.pop();

					// TODO - probably should use the same mechanism used by
					// CALLAT here, since this
					// isn't really quite an internal operation

					//internalParams[2][1] = fetchByte(instructions, ip + 1);
					//stack.push(doInternal(InternalOp.CALL, ins, 2, callCache, ip));
					Object[] params = new Object[fetchByte(instructions, ip + 1)];
					this.popParams(stack, params);
					stack.push(params);

					// Need to bump ip BEFORE calling next method.
					// Otherwise,
					// the ip will be stored in its old state and when this
					// method resumes after being suspended, it will try to
					// re-run this call.
					ip += 2;
					stack.push(callExternal(stack, ins, "call", 1, callCache, ip));
					break;
				case CALLAT:
					ins = stack.pop();

					String methodName = (String) constantPool[fetchInt(instructions, ip + 2)];


					// TODO - this is not an internal operation, so we need
					// a different caching mechanism
					// here
					int paramCount = fetchByte(instructions, ip + 1);

					// Need to bump ip BEFORE calling next method.
					// Otherwise,
					// the ip will be stored in its old state and when this
					// method resumes after being suspended, it will try to
					// re-run this call.
					ip += 6;
					stack.push(callExternal(stack, ins, methodName, paramCount, callCache, ip));
					break;
				case GOTO:
					int gotoIndex = fetchInt(instructions, ip + 1);
					ip = gotoIndex;
					break;
				case THROW:
					ins = stack.pop();
					throw new ExceptionChipmunk(ins);
				case RETURN:
					ins = stack.pop();
					return ins;
				case POP:
					ins = stack.pop();
					ip++;
					break;
				case DUP:
					int dupIndex = fetchInt(instructions, ip + 1);
					stack.dup(dupIndex);
					ip += 5;
					break;
				case SWAP:
					int swapIndex1 = fetchInt(instructions, ip + 1);
					int swapIndex2 = fetchInt(instructions, ip + 5);
					stack.swap(swapIndex1, swapIndex2);
					ip += 9;
					break;
				case PUSH:
					int constIndex = fetchInt(instructions, ip + 1);
					Object constant = constantPool[constIndex];
					stack.push(constant);
					ip += 5;
					break;
				case EQ:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.EQUALS, lh, 2, callCache, ip));
					ip++;
					break;
				case GT:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() > 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case LT:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() < 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case GE:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() >= 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case LE:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					if (((CInteger) doInternal(InternalOp.COMPARE, lh, 2, callCache, ip)).getValue() <= 0) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case IS:
					rh = stack.pop();
					lh = stack.pop();
					if (lh == rh) {
						stack.push(trueValue);
					} else {
						stack.push(falseValue);
					}
					ip++;
					break;
				case INSTANCEOF:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[2][1] = rh;
					stack.push(doInternal(InternalOp.INSTANCEOF, lh, 2, callCache, ip));
					ip++;
					break;
				case ITER:
					ins = stack.pop();
					stack.push(doInternal(InternalOp.ITERATOR, ins, 1, callCache, ip));
					ip++;
					break;
				case NEXT:
					ins = stack.peek();
					if (!((CIterator) ins).hasNext(this)) {
						// pop the iterator
						stack.pop();
						ip = fetchInt(instructions, ip + 1);
					} else {

						stack.push(doInternal(InternalOp.NEXT, ins, 1, callCache, ip));
						ip += 5;
					}
					break;
				case RANGE:
					rh = stack.pop();
					lh = stack.pop();
					internalParams[3][1] = rh;
					internalParams[3][2] = fetchByte(instructions, ip + 1) == 0 ? false : true;
					stack.push(doInternal(InternalOp.RANGE, lh, 3, callCache, ip));
					ip += 2;
					break;
				case LIST:
					int elementCount = fetchInt(instructions, ip + 1);
					CList list = new CList();
					for(int i = 0; i < elementCount; i++){
						list.add(this, stack.pop());
					}
					// elements are popped in reverse order
					list.reverse();
					traceMem(8);
					stack.push(list);
					ip += 5;
					break;
				case MAP:
					elementCount = fetchInt(instructions, ip + 1);
					CMap map = new CMap();
					for(int i = 0; i < elementCount; i++){
						Object value = stack.pop();
						Object key = stack.pop();
						map.put(this, key, value);
					}
					traceMem(8);
					stack.push(map);
					ip += 5;
					break;
				case INIT:
					ins = stack.pop();
					stack.push(((Initializable) ins).getInitializer());
					ip++;
					break;
				case GETMODULE:
					ins = method.getCode().getModule().getNamespace()
							.get((String)constantPool[fetchInt(instructions, ip + 1)]);
					if(ins == null) {
						stack.push(CNull.instance());
					}else {
						stack.push(ins);
					}
					ip += 5;
					break;
				case SETMODULE:
					ins = stack.peek();
					method.getCode().getModule()
					.getNamespace()
					.set(constantPool[
							fetchInt(instructions, ip + 1)].toString(),
							ins);
					ip += 5;
					break;
				case INITMODULE:
					int importIndex = fetchInt(instructions, ip + 1);
					ip += 5;
					initModule(method.getCode(), importIndex);
					break;
				case IMPORT:
					importIndex = fetchInt(instructions, ip + 1);
					ip += 5;
					doImport(method.getCode(), importIndex);
					break;
				default:
					throw new InvalidOpcodeChipmunk(op);
				}

			} catch (RuntimeException e) {
				
				// SuspendedChipmunk must be re-thrown - don't
				// allow exception handlers to run or they will
				// block suspension
				if (e instanceof SuspendedChipmunk) {
					this.freeze(method, ip, locals, stack);
					throw e;
				}

				// Wrap all native exceptions as Chipmunk exceptions, add trace info, and propagate
				if (!(e instanceof AngryChipmunk)) {
					e = new AngryChipmunk(e.getMessage(), e);
				}
				
				AngryChipmunk ex = (AngryChipmunk) e;
				
				CTraceFrame trace = new CTraceFrame();
				trace.setDebugSymbol(method.getDebugSymbol());
				trace.lineNumber = findLineNumber(ip, method.getCode().getDebugTable());

				ex.addTraceFrame(trace);
				
				ExceptionBlock handler = chooseExceptionHandler(ip, method.getCode().getExceptionTable());
				if(handler != null) {
					ip = handler.catchIndex;
					locals[handler.exceptionLocalIndex] = e;
				}else {
					throw e;
				}
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

	private void initModule(CMethodCode code, int importIndex){
		CModule.Import im = code.getModule().getImports().get(importIndex);
		if(!modules.containsKey(im.getName())){
			CModule newModule = loadModule(im.getName());
			if(newModule.hasInitializer()){
				this.dispatch(newModule.getInitializer(), null);
			}
		}
	}

	private void doImport(CMethodCode code, int importIndex){
		final CModule module = code.getModule();
		final CModule.Import moduleImport = code.getModule().getImports().get(importIndex);
		final Namespace importedNamespace = modules.get(moduleImport.getName()).getNamespace();

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

	public Object lookupMethod(Object target, String opName, Class<?>[] callTypes) throws Throwable {

		Method[] methods = target.getClass().getMethods();
		Method method = null;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(opName)) {
				// only call public methods
				if (paramTypesMatch(methods[i].getParameterTypes(), callTypes)
						&& ((methods[i].getModifiers() & Modifier.PUBLIC) != 0)) {

					method = methods[i];
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
				throw e;
			}catch(Throwable e) {
				throw e;
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
	
	private void popParams(OperandStack stack, Object[] params) {
		for(int i = params.length - 1; i >= 0; i--) {
			params[i] = stack.pop();
		}
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
				
				//method = getOrCacheInternal(op, target, params, callCache, callCacheIndex);
				Object retVal = invoke(method, target, params);
				// the following is helpful when weird bugs crop up - it nulls the parameter array after use
				// Arrays.fill(params, null);
				return retVal != null ? retVal : CNull.instance();
			}catch(NoSuchMethodException ex) {
				throw new AngryChipmunk(ex);
			}catch(AngryChipmunk ex) {
				throw ex;
			}catch(Throwable ex) {
				throw new AngryChipmunk(ex);
			}
			
		} catch (AngryChipmunk e) {
			throw e;
		} catch (Throwable e) {
			throw new AngryChipmunk(e);
		}
		
	}

	private Object callExternal(OperandStack stack, Object target, String methodName, int paramCount, Object[] callCache, int callCacheIndex) {

		final boolean isInterceptor = target instanceof CallInterceptor;
		final boolean isRuntimeObject = target instanceof RuntimeObject;

		// Assume that the target is not a call interceptor and that it
		// is a runtime object
		final Object[] params = new Object[paramCount + (isRuntimeObject ? 1 : 0)];
		stack.popArgs(paramCount, params);

		if(isRuntimeObject){
			params[0] = this;
		}

		final Object[] interceptedParams = isInterceptor ? Arrays.copyOfRange(params, 1, params.length) : params;

		if(isInterceptor){
			Object result = ((CallInterceptor) target).callAt(this, methodName, interceptedParams);
			// null result indicates that the interceptor did not intercept the call, so
			// continue with a plain dispatch
			if(result != null){
				return result;
			}
		}
		
		Class<?>[] paramTypes = new Class<?>[params.length];

		for (int i = 0; i < params.length; i++) {
			paramTypes[i] = params[i].getClass();
		}

		try {
			if(callCache[callCacheIndex] == null) {
				Object invokeTarget = lookupMethod(target, methodName, paramTypes);
				callCache[callCacheIndex] = invokeTarget;
				Object retVal = invoke(invokeTarget, target, params);
				
				return retVal != null ? retVal : CNull.instance();
			}else {
				// TODO - need to check parameter types, call target, etc. and verify that the call is valid
				Object invokeTarget = callCache[callCacheIndex];
				Object retVal = invoke(invokeTarget, target, params);
				
				return retVal != null ? retVal : CNull.instance();
			}
			
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new AngryChipmunk(e);
		} catch (AngryChipmunk e) {
			throw e;
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
	
	private Object getOrCacheInternal(InternalOp op, Object target, Object[] params, Object[] callCache, int callCacheIndex) throws Throwable {
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
	
	private ExceptionBlock chooseExceptionHandler(int ip, ExceptionBlock[] eTable) {
		ExceptionBlock lastCandidate = null;
		
		if(eTable == null) {
			return null;
		}
		
		for(ExceptionBlock block : eTable) {
			if(lastCandidate == null) {
				lastCandidate = block;
			}else if(ip >= block.startIndex && ip <= block.catchIndex && block.startIndex > lastCandidate.startIndex && block.catchIndex < lastCandidate.catchIndex){
				lastCandidate = block;
			}
		}

		return lastCandidate;
	}
	
	private int findLineNumber(int ip, DebugEntry[] debugTable) {

		for(DebugEntry dbg : debugTable) {
			
			if(ip >= dbg.beginIndex && ip < dbg.endIndex) {
				return dbg.lineNumber;
			}
		}
		
		return 0;
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
