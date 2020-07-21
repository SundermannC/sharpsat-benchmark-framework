package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class IterativeBUNode {
	
	protected List<IterativeBUNode> children = new ArrayList<IterativeBUNode>();
	
	public BigInteger overallModelCount;
	
	public BigInteger currentModelCount;
	
	public BigInteger tempModelCount;
	
	public boolean changedValue;
	
	public abstract void addChild(IterativeBUNode child);
	
	public abstract void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded);
	
	public abstract void ursPropagateTempChange(Integer variable);
	
	public abstract void ursPropagateChange(Integer variable);
	
	public abstract void propagateCommonality(int variable);
	
	public void saveUnsureResult() {
		currentModelCount = tempModelCount;
	}
	
	public void resetCurrentModelCount() {
		currentModelCount = overallModelCount;
	}
}
