package ddnnfparsing.bottomup;

import java.math.BigInteger;

public class BottomupAnd extends BottomupDdnnfNode {

	public BottomupAnd()  {
		overallModelCount = BigInteger.ONE;
	}
	
	@Override
	public void propagateChange(BigInteger newCount, BigInteger oldCount) {
		// New count is always less or equal than old count. Thus, if oldcount was 0, newcount is always 0
		// tempModel count of this and node is also always 0 in this case and does not need to be changed
		// Thus we can easily skip the computation to prevent division by zero :)
		// Also there are no changes during the traverse to the path whatsoever
		if (oldCount.equals(BigInteger.ZERO)) {
			return;
		} else {
			BigInteger intermediate = tempModelCount.divide(oldCount);
			BigInteger result = intermediate.multiply(newCount);
			for (BottomupDdnnfNode parent : parents) {
				parent.propagateChange(result, tempModelCount);
			}
			tempModelCount = result;			
		}
	}

	@Override
	public void propagateUnsureChange(BigInteger newCount, BigInteger oldCount) {
		// New count is always less or equal than old count. Thus, if oldcount was 0, newcount is always 0
		// tempModel count of this and node is also always 0 in this case and does not need to be changed
		// Thus we can easily skip the computation to prevent division by zero :)
		// Also there are no changes during the traverse to the path whatsoever
		if (oldCount.equals(BigInteger.ZERO)) {
			return;
		} else {
			BigInteger intermediate = tempModelCount.divide(oldCount);
			BigInteger result = intermediate.multiply(newCount);
			for (BottomupDdnnfNode parent : parents) {
				parent.propagateUnsureChange(result, tempModelCount);
			}
			unsureTempModelCount = result;			
		}
		
	}
	
	public void addChild(BottomupDdnnfNode child) {
		overallModelCount = overallModelCount.multiply(child.overallModelCount);
	}
	
	
}
