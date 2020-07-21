package ddnnfparsing.iterativebottomup;

import java.math.BigInteger;
import java.util.Set;

public class IterativeDecision extends IterativeBUNode {

	public IterativeDecision(int variable) {
		overallModelCount = BigInteger.ZERO;
		this.variable = variable;
	}

	int variable;
	
	IterativeBUNode leftChild;
	IterativeBUNode rightChild;
	
	
	

	@Override
	public void getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		if (included.contains(variable)) {
			tempModelCount = leftChild.tempModelCount;
			changedValue = true;
		} else if(excluded.contains(variable)) {
			tempModelCount = rightChild.tempModelCount;
			changedValue = true;
		} else {
			changedValue = false;
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
		if (this.variable == variable) {
			if (leftChild.changedValue) {
				tempModelCount = leftChild.tempModelCount;
			} else {
				tempModelCount = leftChild.overallModelCount;
			}
			changedValue = true;
		} else {
			if (leftChild.changedValue) {
				if (rightChild.changedValue) {
					tempModelCount = leftChild.tempModelCount.add(rightChild.tempModelCount);
					changedValue = true;
				} else {
					tempModelCount = leftChild.tempModelCount.add(rightChild.overallModelCount);
					changedValue = true;
				}
			} else if (rightChild.changedValue) {
				tempModelCount = leftChild.overallModelCount.add(rightChild.tempModelCount);
				changedValue = true;
			} else {
				changedValue = false;
			}
		}
		
	}
	
	public void addLeftChild(IterativeBUNode child) {
		leftChild = child;
		overallModelCount = overallModelCount.add(child.overallModelCount);
	}
	
	public void addRightChild(IterativeBUNode child) {
		rightChild = child;
		overallModelCount = overallModelCount.add(child.overallModelCount);
	}

}
