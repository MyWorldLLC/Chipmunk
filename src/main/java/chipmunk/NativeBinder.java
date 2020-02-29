package chipmunk;

import chipmunk.modules.runtime.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class NativeBinder implements RuntimeObject, CallInterceptor {

    protected final Object bound;

    public NativeBinder(Object bound){
        if(bound == null){
            throw new NullPointerException("Bound object must not be null");
        }
        this.bound = bound;
    }

    public Object getBoundObject(){
        return bound;
    }

    public Object getAttr(ChipmunkVM vm, CString name){
        Field f = getField(name);
        try {
            return mapToCType(vm, f.get(bound));
        } catch (IllegalAccessException e) {
            throw new AngryChipmunk(e);
        }
    }

    public Object setAttr(ChipmunkVM vm, CString name, Object value){
        Field f = getField(name);
        try {
            Object o = value;
            if(!f.getType().equals(value.getClass())){
                o = mapFromCType(vm, f, value);
            }
            f.set(bound, o);
            return value;
        } catch (IllegalAccessException e) {
            throw new AngryChipmunk(e);
        }
    }

    protected Object mapToCType(ChipmunkVM vm, Object o){

        if(o == null){
            return CNull.instance();
        }

        Class<?> cls = o.getClass();
        if(cls.equals(Integer.class)){
            return new CInteger((Integer) o);
        }else if(cls.equals(Float.class)){
            return new CFloat((Float) o);
        }else if(cls.equals(String.class)){
            return new CString((String) o);
        }else if(cls.equals(Boolean.class)){
            return new CBoolean((Boolean) o);
        }else if(o instanceof List<?>){
            return new CList((List) o);
        }else if(o instanceof Map<?,?>){
            return new CMap((Map) o);
        }
        return new NativeBinder(o);
    }

    protected Object mapFromCType(ChipmunkVM vm, Field f, Object o){
        if(o instanceof CNull){
            return null;
        }

        Class<?> cls = o.getClass();
        if(cls.equals(CInteger.class)){
            return ((CInteger) o).intValue();
        }else if(cls.equals(CFloat.class)){
            return ((CFloat) o).floatValue();
        }else if(cls.equals(CString.class)){
            return ((CString) o).stringValue();
        }else if(cls.equals(Boolean.class)){
            return ((CBoolean) o).booleanValue();
        }else if(cls.equals(CList.class)){
            return ((CList) o).getBackingList();
        }else if(cls.equals(CMap.class)){
            return ((CMap) o).getBackingMap();
        }
        return o;
    }

    protected Field getField(CString name){
        try {
            Field field = bound.getClass().getField(name.stringValue());

            if((field.getModifiers() & Field.PUBLIC) == 0) {
                throw new IllegalAccessException(String.format("Field %s of class %s is not public", name, bound.getClass().getName()));
            }

            return field;
        } catch (NoSuchFieldException e) {
            throw new MissingVariableChipmunk(String.format("No such variable %s for native class %s", name, bound.getClass().getName()));
        } catch(IllegalAccessException e){
            throw new AngryChipmunk(e);
        }
    }

    @Override
    public Object callAt(ChipmunkVM vm, String methodName, Object[] params) {
        Class<?>[] paramTypes = new Class<?>[params.length];
        for(int i = 0; i < params.length; i++){
            Object o = params[i];
            if(o != null){
                params[i] = o.getClass();
            }
        }

        try {
            Method method = bound.getClass().getMethod(methodName, paramTypes);
            if((method.getModifiers() & Field.PUBLIC) == 0) {
                throw new IllegalAccessException(String.format("Method %s of class %s is not public", methodName, bound.getClass().getName()));
            }

            Object o = method.invoke(bound, (Object)params);
            return mapToCType(vm, o);
        } catch (NoSuchMethodException e) {
            throw new MissingVariableChipmunk(String.format("No such method %s for native class %s", methodName, bound.getClass().getName()));
        } catch(IllegalAccessException | InvocationTargetException e){
            throw new AngryChipmunk(e);
        }
    }
}
