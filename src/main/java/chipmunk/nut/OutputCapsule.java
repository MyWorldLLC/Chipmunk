package chipmunk.nut;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class OutputCapsule {

	private DataOutputStream os;
	
	public OutputCapsule(OutputStream out){
		os = new DataOutputStream(out);
	}
	
	public void write(byte b){
		try {
			os.write(b);
		} catch (IOException ex){
			throw new IOChipmunk("Error writing to output stream", ex);
		}
	}
	
	public void write(byte[] b){
		write(b, 0, b.length);
	}
	
	public void write(byte[] b, int begin, int end){
		try {
			os.write(b, begin, end);
		} catch (IOException ex){
			throw new IOChipmunk("Error writing to output stream", ex);
		}
	}
	
	public void write(int i){
		try {
			os.writeInt(i);
		} catch (IOException ex){
			throw new IOChipmunk("Error writing to output stream", ex);
		}
	}
	
	public void write(float f){
		try {
			os.writeFloat(f);
		} catch (IOException ex){
			throw new IOChipmunk("Error writing to output stream", ex);
		}
	}
	
	public void write(boolean b){
		if(b){
			write(0x01);
		}else{
			write(0x00);
		}
	}
	
	public void write(String s){
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		write(bytes.length);
		write(bytes);
	}
	
	protected void close(){
		try {
			os.close();
		} catch (IOException ex){
			throw new IOChipmunk("Error closing output stream", ex);
		}
	}
}
