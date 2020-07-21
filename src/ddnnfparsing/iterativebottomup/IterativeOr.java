package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeOr extends IterativeBUNode {

	public IterativeOr() {
		overallModelCount = BigInteger.ZERO;
	}


	@Override
	public void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		changedValue = false;
		for (IterativeBUNode child : children) {
			if (child.changedValue) {
				changedValue = true;
				break;
			}
		}
		if (changedValue) {
			tempModelCount = BigInteger.ZERO;
			for (IterativeBUNode child :children) {
				if (child.changedValue) {
					tempModelCount = tempModelCount.add(child.tempModelCount);					
				} else {
					tempModelCount = tempModelCount.add(child.overallModelCount);
				}
				
			}		
		}
	}

	@Override
	public void ursPropagateTempChange(Integer variable) {
		changedValue = false;
		for (IterativeBUNode child : children) {
			if (child.changedValue) {
				changedValue = true;
				break;
			}
		}
		if (changedValue) {
			tempModelCount = BigInteger.ZERO;
			for (IterativeBUNode child :children) {
				tempModelCount = tempModelCount.add(child.tempModelCount);
			}		
		} else {
			tempModelCount = overallModelCount;
		}
	}

	@Override
	public void ursPropagateChange(Integer variable) {
		changedValue = false;
		for (IterativeBUNode child : children) {
			if (child.changedValue) {
				changedValue = true;
				break;
			}
		}
		if (changedValue) {
			currentModelCount = BigInteger.ZERO;
			for (IterativeBUNode child :children) {
				currentModelCount = currentModelCount.add(child.tempModelCount);
			}		
		}
	}


	@Override
	public void addChild(IterativeBUNode child) {
		children.add(child);
		overallModelCount = overallModelCount.add(child.overallModelCount);
		
	}


	@Override
	public void propagateCommonality(int variable) {
		changedValue = false;
		for (IterativeBUNode child : children) {
			if (child.changedValue) {
				changedValue = true;
				break;
			}
		}
		if (changedValue) {
			tempModelCount = BigInteger.ZERO;
			for (IterativeBUNode child :children) {
				if (child.changedValue) {
					tempModelCount = tempModelCount.add(child.tempModelCount);					
				} else {
					tempModelCount = tempModelCount.add(child.overallModelCount);		
				}
			}		
		}
		
	}

}
