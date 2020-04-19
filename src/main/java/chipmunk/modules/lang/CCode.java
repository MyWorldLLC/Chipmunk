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
