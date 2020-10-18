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

package chipmunk.modules.runtime;

import chipmunk.AngryChipmunk;

public class TypeConversionException extends AngryChipmunk {

	private static final long serialVersionUID = 2673936336954560112L;
	
	private final Object obj;
	private final Class<?> targetClass;
	
	public TypeConversionException(){
		this(null, null, null);
	}
	
	public TypeConversionException(String msg){
		this(msg, null, null);
	}
	
	public TypeConversionException(String msg, Object obj, Class<?> targetClass){
		super(msg);
		this.obj = obj;
		this.targetClass = targetClass;
	}
	
	public Object getObject(){
		return obj;
	}
	
	public Class<?> getTargetClass(){
		return targetClass;
	}
}
