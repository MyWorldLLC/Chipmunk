package chipmunk.truffle;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;

public class ChipmunkContext {
	
	private final ChipmunkLanguage language;
	private final Env env;
	
	public ChipmunkContext(ChipmunkLanguage language, TruffleLanguage.Env env) {
		this.language = language;
		this.env = env;
	}
	
	public Env getEnvironment() {
		return env;
	}
	
	public ChipmunkLanguage getLanguage() {
		return language;
	}
}
