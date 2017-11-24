package chipmunk.reflectors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import chipmunk.AngryChipmunk;
import chipmunk.ChipmunkContext;

public class Reflector {
	
	private final Object obj;
	
	public Reflector(Object instance){
		obj = instance;
	}
	
	public Object doOp(String op, ChipmunkContext context, Object... params) throws AngryChipmunk {
		Class<?>[] paramTypes = new Class<?>[params.length];
		for(int i = 0; i < params.length; i++){
			paramTypes[i] = params[i].getClass();
		}
		
		Method method = null;
		
		try {
			method = obj.getClass().getMethod(op, paramTypes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		try {
			return method.invoke(obj, params);
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
