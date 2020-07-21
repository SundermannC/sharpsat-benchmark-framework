package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeLiteral extends IterativeBUNode {

	int variable;
	
	public IterativeLiteral(int variable) {
		this.variable = variable;
		overallModelCount = BigInteger.ONE;
		tempModelCount = overallModelCount;
	}
	
	@Override
	public void addChild(IterativeBUNode child) {}

	@Override
	public void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		if (excluded.contains(variable)) {
			tempModelCount = BigInteger.ZERO;
			changedValue = true;
		} else {
			changedValue = false;
		}
	}

	@Override
	public void ursPropagateTempChange(Integer variable) {
		changedValue = true;
		tempModelCount = BigInteger.ZERO;
	}

	@Override
	public void ursPropagateChange(Integer variable) {
		changedValue = false;
	}

	@Override
	public void propagateCommonality(int variable) {
		changedValue = false;
	}

}
