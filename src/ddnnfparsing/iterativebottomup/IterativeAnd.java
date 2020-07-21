package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeAnd extends IterativeBUNode {
	
	
	
	public IterativeAnd() {
		overallModelCount = BigInteger.ONE;
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
			tempModelCount = BigInteger.ONE;
			for (IterativeBUNode child : children) {
				if (child.changedValue) {
					tempModelCount = tempModelCount.multiply(child.tempModelCount);					
				} else {
					tempModelCount = tempModelCount.multiply(child.overallModelCount);
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
			tempModelCount = BigInteger.ONE;
			for (IterativeBUNode child : children) {
				tempModelCount = tempModelCount.multiply(child.tempModelCount);
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
			currentModelCount = BigInteger.ONE;
			for (IterativeBUNode child : children) {
				currentModelCount = currentModelCount.multiply(child.tempModelCount);
			}			
		}
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
			tempModelCount = BigInteger.ONE;
			for (IterativeBUNode child : children) {
				if (child.changedValue) {
					tempModelCount = tempModelCount.multiply(child.tempModelCount);
				} else {
					tempModelCount = tempModelCount.multiply(child.overallModelCount);
				}
			}			
		}
		
	}

	@Override
	public void addChild(IterativeBUNode child) {
		children.add(child);
		overallModelCount = overallModelCount.multiply(child.overallModelCount);
		
	}




}
