package ddnnfparsing.optimized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DdnnfAnd extends DdnnfNode {
	
	public DdnnfAnd() {
		super(BigInteger.ONE);
		childrenVariables = new ArrayList<Set<Integer>>();
	}
	
	List<Set<Integer>> childrenVariables;
	

	@Override
	public BigInteger computePartialConfiguration(Set<Integer> included, Set<Integer> excluded) {
		BigInteger result = BigInteger.ONE;
		for (int i = 0; i < children.size(); i++) {
			if (!Collections.disjoint(childrenVariables.get(i), included) || !Collections.disjoint(childrenVariables.get(i), excluded)) {
				result = result.multiply(children.get(i).computePartialConfiguration(included, excluded));
			} else {
				result = result.multiply(children.get(i).overallModelCount);
			}
		}
		
		return result;
	}
	
	@Override
	public void addChild(DdnnfNode node) {
		super.addChild(node);
		childrenVariables.add(node.getVariables());
		overallModelCount = overallModelCount.multiply(node.overallModelCount);
	}
	
	
	public Set<Integer> getVariables() {
		Set<Integer> variables = new HashSet<>();
		for (Set<Integer> childVariables : childrenVariables) {
			variables.addAll(childVariables);
		}
		return variables;
	}

}
