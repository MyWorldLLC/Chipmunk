package chipmunk.modules.lang;

public class CCode extends CObject {
	
	protected byte[] code;
	
	public CCode(){
		code = new byte[]{};
	}
	
	public CCode(byte[] code){
		this.code = code;
	}
	
	public void setCode(byte[] code){
		this.code = code;
	}
	
	public byte[] getCode(){
		return code;
	}
	
	public boolean equals(Object other){
		if(other == null){
			return false;
		}
		
		if(other instanceof CCode){
			
			CCode otherCode = (CCode) other;
			byte[] otherCodeArray = otherCode.getCode();
			
			if(otherCodeArray.length == code.length){
				
				for(int i = 0; i < code.length; i++){
					if(otherCodeArray[i] != code[i]){
						return false;
					}
				}
				
				return true;
			}
		}
		return false;
	}

}
