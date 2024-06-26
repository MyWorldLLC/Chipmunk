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

package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

public class TryCatchLabels extends BlockLabels {
	
	protected final List<BlockLabels> catchBlocks;
	protected final String successTarget;
	protected boolean successMarked;
	
	public TryCatchLabels(String start, String end, String successTarget) {
		super(start, end);
		catchBlocks = new ArrayList<>();
		this.successTarget = successTarget;
		successMarked = false;
	}
	
	public List<BlockLabels> getCatchBlocks(){
		return catchBlocks;
	}

	public String getSuccessTarget(){
		return successTarget;
	}

	public void setSuccessMarked(){
		successMarked = true;
	}

	public boolean isSuccessMarked(){
		return successMarked;
	}
}
