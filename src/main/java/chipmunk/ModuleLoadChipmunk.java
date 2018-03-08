package chipmunk;

public class ModuleLoadChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = -4373636324025801028L;

	public ModuleLoadChipmunk(){
		this(null, null);
	}
	
	public ModuleLoadChipmunk(String message){
		this(message, null);
	}

	public ModuleLoadChipmunk(Throwable cause){
		this(cause.getMessage(), cause);
	}
	
	public ModuleLoadChipmunk(String message, Throwable cause){
		super(message, cause);
	}
}
