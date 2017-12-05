package chipmunk.reflectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chipmunk.AngryChipmunk;
import chipmunk.ChipmunkVM;
import chipmunk.modules.reflectiveruntime.MissingMethodChipmunk;

public class Reflector {
	
	private final Object obj;
	
	public Reflector(Object instance){
		obj = instance;
	}
	
	public Object getObject(){
		return obj;
	}
	
	public Reflector doOp(ChipmunkVM context, String op, Object... params) throws AngryChipmunk {
		// convert reflectors to their actual types
		for(int i = 0; i < params.length; i++){
			if(params[i] instanceof Reflector){
				params[i] = ((Reflector) params[i]).getObject();
			}
		}
		
		// get parameter type list to match method signature
		Class<?>[] paramTypes = new Class<?>[params.length];
		for(int i = 0; i < params.length; i++){
			paramTypes[i] = params[i].getClass();
		}
		
		Method method = null;
		
		try {
			method = obj.getClass().getMethod(op, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new MissingMethodChipmunk(e);
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		try {
			// TODO - need this to call anonymous classes (like iterators), but we probably don't want this all the time
			method.setAccessible(true);
			Object result = method.invoke(obj, params);
			if(result instanceof VMOperator){
				return new VMReflector((VMOperator) result);
			}else{
				return new Reflector(result);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

}
