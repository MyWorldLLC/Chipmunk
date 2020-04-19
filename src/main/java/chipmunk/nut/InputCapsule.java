/*
 * Copyright (C) 2020 MyWorld, LLC
 * All rights reserved.
 *
 * This file is part of Chipmunk.
 *
 * Chipmunk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chipmunk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chipmunk.  If not, see <https://www.gnu.org/licenses/>.
 */

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
