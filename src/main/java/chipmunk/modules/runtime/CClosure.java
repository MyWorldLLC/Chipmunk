package chipmunk.modules.runtime;

public class CClosure implements RuntimeObject {
	
	protected boolean isFinal;
	protected Object value;
	
	public CClosure() {
		this(false);
	}
	
	public CClosure(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	public void set(Object value) {
		if(!isFinal || this.value == null) {
			this.value = value;
		}else {
			throw new FinalViolationChipmunk();
		}
	}
	
	public Object get() {
		return value != null ? value : CNull.instance();
	}

}
