package chipmunk.reflectors;

import chipmunk.ChipmunkVM;

public class VMReflector extends Reflector {

	public VMReflector(VMOperator instance) {
		super(instance);
	}
	
	@Override
	public Reflector doOp(ChipmunkVM context, String op, Object... params){
		Object[] fullParams = new Object[params.length + 1];
		fullParams[0] = context;
		for(int i = 1, j = 0; j < params.length; i++, j++){
			fullParams[i] = params[j];
		}
		return super.doOp(context, op, fullParams);
	}

}
