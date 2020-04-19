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

package chipmunk;

public class NativeChipmunk extends AngryChipmunk {

	private static final long serialVersionUID = 4862801255426666976L;
	
	protected Exception nativeException;
	
	public NativeChipmunk(Exception ex){
		this(ex.getMessage(), ex);
	}
	
	public NativeChipmunk(String msg, Exception ex){
		super(msg, ex);
		nativeException = ex;
	}
	
	public Exception getNativeException(){
		return nativeException;
	}
}
