package chipmunk.truffle;

import com.oracle.truffle.api.TruffleLanguage;

public class ChipmunkLanguage extends TruffleLanguage<ChipmunkContext> {
	
	public ChipmunkLanguage() {
		super();
	}

	@Override
	protected ChipmunkContext createContext(Env env) {
		return new ChipmunkContext(this, env);
	}

	@Override
	protected boolean isObjectOfLanguage(Object object) {
		// TODO
		return object instanceof Integer 
				|| object instanceof Boolean 
				|| object instanceof Long 
				|| object instanceof String 
				|| object instanceof Object[];
	}

}
