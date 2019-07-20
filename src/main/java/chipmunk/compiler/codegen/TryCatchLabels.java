package chipmunk.compiler.codegen;

import java.util.ArrayList;
import java.util.List;

public class TryCatchLabels extends BlockLabels {
	
	protected final List<BlockLabels> catchBlocks;
	
	public TryCatchLabels(String start, String end) {
		super(start, end);
		catchBlocks = new ArrayList<>();
	}
	
	public List<BlockLabels> getCatchBlocks(){
		return catchBlocks;
	}
}
