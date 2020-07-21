package ddnnfparsing.bottomup;

import java.math.BigInteger;

/**
 * Ddnnf node for bottom up analysis that represents False
 * This is only required for the initial computation and is removed from the tree afterwards
 * @author chico
 *
 */
public class BottomupFalse extends BottomupDdnnfNode {

	
	public BottomupFalse() {
		overallModelCount = BigInteger.ZERO;
	}
	
	@Override
	public void propagateChange(BigInteger newCount, BigInteger old) {
		// TODO Auto-generated method stub

	}

	@Override
	public void propagateUnsureChange(BigInteger newCount, BigInteger oldCount) {
	}

	
	@Override
	public void addParent(BottomupDdnnfNode parent) {
		return;
	}
}
