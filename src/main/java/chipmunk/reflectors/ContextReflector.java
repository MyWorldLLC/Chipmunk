package chipmunk.reflectors;

import chipmunk.ChipmunkContext;

public class ContextReflector extends Reflector {

	public ContextReflector(ContextOperator instance) {
		super(instance);
	}
	
	@Override
	public Reflector doOp(ChipmunkContext context, String op, Object... params){
		Object[] fullParams = new Object[params.length + 1];
		fullParams[0] = context;
		for(int i = 1, j = 0; j < params.length; i++, j++){
			fullParams[i] = params[j];
		}
		return super.doOp(context, op, fullParams);
	}

}
