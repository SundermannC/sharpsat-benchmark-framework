package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeNot extends IterativeBUNode {

	int variable;
	
	public IterativeNot(int variable) {
		this.variable = variable;
		overallModelCount = BigInteger.ONE;
		tempModelCount = overallModelCount;
	}
	
	@Override
	public void addChild(IterativeBUNode child) {}

	@Override
	public void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		if (included.contains(variable)) {
			tempModelCount = BigInteger.ZERO;
			changedValue = true;
		} else {
			changedValue = false;
		}
	}

	@Override
	public void ursPropagateTempChange(Integer variable) {
		changedValue = false;
	}

	@Override
	public void ursPropagateChange(Integer variable) {
		changedValue = true;
		currentModelCount = BigInteger.ZERO;
	}

	@Override
	public void propagateCommonality(int variable) {
		if (this.variable == variable) {
			changedValue = true;
			tempModelCount = BigInteger.ZERO;			
		} else {
			changedValue = false;
		}
	}

}
