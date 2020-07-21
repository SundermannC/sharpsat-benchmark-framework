package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeFalse extends IterativeBUNode {

	public IterativeFalse() {
		overallModelCount = BigInteger.ZERO;
		changedValue = false;
	}
	
	@Override
	public void addChild(IterativeBUNode child) {}

	@Override
	public void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {}

	@Override
	public void ursPropagateTempChange(Integer variable) {}

	@Override
	public void ursPropagateChange(Integer variable) {}

	@Override
	public void propagateCommonality(int variable) {}

}
