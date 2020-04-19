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

public class LangModule extends CModule {

	public LangModule(){
		super("chipmunk.lang");
		
		CNull nullObject = new CNull();
		nullObject.getType().setModule(this);
		namespace.set("null", CNullType.nullObject);
		
		CStringType stringType = new CStringType();
		stringType.setModule(this);
		namespace.set("String", stringType);
		
		CFloatType floatType = new CFloatType();
		floatType.setModule(this);
		namespace.set("float", floatType);
		
		CIntType intType = new CIntType();
		intType.setModule(this);
		namespace.set("int", intType);
		
		CBooleanType boolType = new CBooleanType();
		boolType.setModule(this);
		namespace.set("boolean", boolType);
		
		CMethodType methodType = new CMethodType();
		methodType.setModule(this);
		namespace.set("Method", methodType);
	}
	
}