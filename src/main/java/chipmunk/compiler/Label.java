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

package chipmunk.compiler;

public class Label {
	
	private String name;
	private int codeIndex;

	public Label(String name, int codeIndex){
		this.name = name;
		this.codeIndex = codeIndex;
	}
	
	public String getName(){
		return name;
	}
	
	public int getCodeIndex(){
		return codeIndex;
	}
	
	public boolean equals(Object other){
		
		if(other instanceof Label){
			Label otherLabel = (Label) other;
			
			if(name.equals(otherLabel.name)){
				return true;
			}
		}
		
		return false;
	}
	
	public String toString(){
		return name + ":" + codeIndex;
	}
}
