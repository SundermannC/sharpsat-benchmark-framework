package ddnnfparsing.optimized;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class DdnnfOr extends DdnnfNode {

	public DdnnfOr() {
		super(BigInteger.ZERO);
	}

	@Override
	public BigInteger computePartialConfiguration(Set<Integer> included, Set<Integer> excluded) {
		BigInteger result = BigInteger.ZERO;
		for (DdnnfNode child : children) {
			result = result.add(child.computePartialConfiguration(included, excluded));
			
		}
		return result;
	}
	
	
	@Override
	public void addChild(DdnnfNode node) {
		children.add(node);
		overallModelCount = overallModelCount.add(node.overallModelCount);
	}

	@Override
	public Set<Integer> getVariables() {
		HashSet<Integer> variables = new HashSet<>();
		for (DdnnfNode child : children) {
			variables.addAll(child.getVariables());
		}
		return variables;
	}
	
	

}
