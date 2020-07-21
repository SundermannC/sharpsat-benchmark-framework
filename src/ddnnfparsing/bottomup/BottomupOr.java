package ddnnfparsing.bottomup;

import java.math.BigInteger;

public class BottomupOr extends BottomupDdnnfNode {

	
	public BottomupOr() {
		overallModelCount = BigInteger.ZERO;
	}
	
	@Override
	public void propagateChange(BigInteger newCount, BigInteger oldCount) {
		BigInteger result = tempModelCount.add(newCount).subtract(oldCount);
		for (BottomupDdnnfNode parent : parents) {
			parent.propagateChange(result, tempModelCount);
		}
		tempModelCount = result;
	}

	@Override
	public void propagateUnsureChange(BigInteger newCount, BigInteger oldCount) {
		BigInteger result = tempModelCount.add(newCount).subtract(oldCount);
		for (BottomupDdnnfNode parent : parents) {
			parent.propagateUnsureChange(result, tempModelCount);
		}
		unsureTempModelCount = result;
	}

	public void addChild(BottomupDdnnfNode child) {
		overallModelCount = overallModelCount.add(child.overallModelCount);
	}

}
