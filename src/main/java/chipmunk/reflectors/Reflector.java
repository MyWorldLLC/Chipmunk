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
			Class<?> objClass = obj.getClass();
			Method[] methods = objClass.getDeclaredMethods();
			for(Method m : methods){
				if(m.getName().equals(op)){
					Class<?>[] mParamTypes = m.getParameterTypes();
					
					if(mParamTypes.length != paramTypes.length){
						continue;
					}
					
					boolean misMatched = false;
					for(int i = 0; i < mParamTypes.length; i++){
						if(!paramTypeMatches(paramTypes[i], mParamTypes[i])){
							misMatched = true;
							break;
						}
					}
					
					if(!misMatched){
						method = m;
						break;
					}
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		if(method == null){
			// method not found
			StringBuilder sb = new StringBuilder();
			sb.append(obj.getClass().getName());
			sb.append('.');
			sb.append(op);
			sb.append('(');
			
			for(int i = 0; i < paramTypes.length; i++){
				sb.append(paramTypes[i].getName());
				if(i < paramTypes.length - 1){
					sb.append(',');
				}
			}
			sb.append(')');
			throw new MissingMethodChipmunk(sb.toString());
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
			throw new AngryChipmunk(e);
		} catch (IllegalArgumentException e) {
			throw new AngryChipmunk(e);
		} catch (InvocationTargetException e) {
			throw new AngryChipmunk(e);
		}
	}
	
	private boolean paramTypeMatches(Class<?> paramType, Class<?> methodType){
		if(paramType.equals(methodType)){
			return true;
		}else if(!paramType.equals(Object.class)){
			// determine if methodType is a superclass of paramType
			// TODO - interfaces
			return paramTypeMatches(paramType.getSuperclass(), methodType);
		}
		return false;
	}

}
