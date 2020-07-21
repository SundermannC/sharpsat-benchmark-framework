package ddnnfparsing.bottomup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class BottomupDdnnfNode {
	
	List<BottomupDdnnfNode> parents = new ArrayList<>();

	
	// This is the count for the subtree without any assignments
	BigInteger overallModelCount;
	
	// This is the count for the subtree under the current set assignment
	BigInteger tempModelCount;
	
	/// This is the count for the subtree under the temporary assignment 
	BigInteger unsureTempModelCount;

	public abstract void propagateChange(BigInteger newCount, BigInteger old);
	
	public abstract void propagateUnsureChange(BigInteger newCount, BigInteger oldCount);
	
	public BigInteger getCount() {
		return overallModelCount;
	}
	
	public void resetTemps() {
		tempModelCount = overallModelCount;
	}
	
	public void saveUnsureResults() {
		tempModelCount = unsureTempModelCount;
		for (BottomupDdnnfNode parent : parents) {
			parent.saveUnsureResults();
		}
	}
	
	public void addParent(BottomupDdnnfNode parent) {
		parents.add(parent);
	}
	
	
}
