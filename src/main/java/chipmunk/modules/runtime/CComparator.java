package chipmunk.modules.runtime;

import java.util.Comparator;

public interface CComparator extends Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1);

}
