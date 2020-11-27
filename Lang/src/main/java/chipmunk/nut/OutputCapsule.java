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
