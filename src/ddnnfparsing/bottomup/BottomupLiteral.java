package ddnnfparsing.bottomup;

import java.math.BigInteger;

public class BottomupLiteral extends BottomupDdnnfNode {

	
	public BottomupLiteral() {
		overallModelCount = BigInteger.ONE;
	}
	
	
	/**
	 * Here comes the magic
	 * This is only called for the leaf literals that correspond to the currently iterated variable
	 * If positive/negative state of both is the same, nothing changes
	 * If it differs the model count is now always zero and always was one. otherwise this would not be called 
	 * This is never traversed again for the sample. Thus, it does not need to be changed
	 */
	@Override
	public void propagateChange(BigInteger newCount, BigInteger old) {
		for (BottomupDdnnfNode parent : parents) {
			parent.propagateChange(BigInteger.ZERO, BigInteger.ONE);
		}
	}

	@Override
	public void propagateUnsureChange(BigInteger newCount, BigInteger oldCount) {
		for (BottomupDdnnfNode parent : parents) {
			parent.propagateUnsureChange(BigInteger.ZERO, BigInteger.ONE);
		}
	}

}
