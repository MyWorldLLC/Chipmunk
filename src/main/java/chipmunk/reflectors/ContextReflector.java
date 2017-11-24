package chipmunk.reflectors;

import chipmunk.ChipmunkContext;

public class ContextReflector extends Reflector {

	public ContextReflector(ContextOperator instance) {
		super(instance);
	}
	
	@Override
	public Object doOp(String op, ChipmunkContext context, Object... params){
		Object[] fullParams = new Object[params.length + 1];
		fullParams[0] = context;
		for(int i = 1; i < params.length; i++){
			fullParams[i] = params[i];
		}
		return super.doOp(op, context, fullParams);
	}

}
