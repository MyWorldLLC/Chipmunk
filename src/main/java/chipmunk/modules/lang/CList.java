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

import java.util.ArrayList;
import java.util.List;

public class CList extends CObject {
	
	protected List<CObject> contents;
	
	public CList(){
		super();
		contents = new ArrayList<CObject>();
	}
	
	@Override
	public CObject __getAt__(CObject index){
		int listIndex = ((CInt) index).getValue();
		// TODO - wrap out-of-bounds exception
		return contents.get(listIndex);
	}
	
	@Override
	public CObject __setAt__(CObject index, CObject value){
		int listIndex = ((CInt) index).getValue();
		// TODO - wrap out-of-bounds exception
		return contents.set(listIndex, value);
	}

}
