package ddnnfparsing.bottomup;

import java.math.BigInteger;

/**
 * Ddnnf node for bottom up analysis that represents True
 * This is only required for the initial computation and is removed from the tree afterwards
 * @author chico
 *
 */
public class BottomupTrue extends BottomupDdnnfNode {

	public BottomupTrue() {
		overallModelCount = BigInteger.ONE;
	}
	
	@Override
	public void propagateChange(BigInteger newCount, BigInteger old) {
		return;
	}

	@Override
	public void propagateUnsureChange(BigInteger newCount, BigInteger oldCount) {
		return;
	}

	
	@Override
	public void addParent(BottomupDdnnfNode parent) {
		return;
	}
}
