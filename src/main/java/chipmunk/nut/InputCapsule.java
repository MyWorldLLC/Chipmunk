package chipmunk.nut;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class InputCapsule {

	protected DataInputStream is;
	
	public InputCapsule(InputStream input){
		is = new DataInputStream(input);
	}
	
	public byte readByte(){
		try{
			return is.readByte();
		}catch(IOException ex){
			throw new IOChipmunk("Error reading from input stream", ex);
		}
	}
	
	public int readInt(){
		try{
			return is.readInt();
		}catch(IOException ex){
			throw new IOChipmunk("Error reading from input stream", ex);
		}
	}
	
	public float readFloat(){
		try{
			return is.readFloat();
		}catch(IOException ex){
			throw new IOChipmunk("Error reading from input stream", ex);
		}
	}
	
	public boolean readBoolean(){
		try{
			byte b = is.readByte();
			if(b == 0x01){
				return true;
			}else if(b == 0x00){
				return false;
			}else{
				throw new NutFormatChipmunk("Poorly formed boolean value in nut. Expected 0 or 1, got " + b);
			}
		}catch(IOException ex){
			throw new IOChipmunk("Error reading from input stream", ex);
		}
	}
	
	public String readString(){
		try{
			int length = is.readInt();
			byte[] utfBytes = new byte[length];
			is.readFully(utfBytes);
			return new String(utfBytes, Charset.forName("UTF-8"));
		}catch(IOException ex){
			throw new IOChipmunk("Error reading from input stream", ex);
		}
	}
	
	protected void close(){
		try {
			is.close();
		} catch (IOException ex){
			throw new IOChipmunk("Error closing input stream", ex);
		}
	}
}
