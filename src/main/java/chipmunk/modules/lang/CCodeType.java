package chipmunk.modules.lang;

import chipmunk.ChipmunkContext;

public class CCodeType extends CType {

	public CCodeType(){
		super("Code");
	}
	@Override
	public CObject instance() {
		return new CCode();
	}
	
	@Override
	public CObject __call__(ChipmunkContext context, int params, boolean resuming){
		if(params == 0){
			return new CCode();
		}else{
			throw new UnimplementedOperationChipmunk("CCodeType.__call__() is not defined for parameter count: " + params);
		}
	}

}
