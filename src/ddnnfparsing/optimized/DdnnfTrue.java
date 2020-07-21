package ddnnfparsing.optimized;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class DdnnfTrue extends DdnnfNode {

	public DdnnfTrue() {
		super(BigInteger.ONE);
	}

	@Override
	public BigInteger computePartialConfiguration(Set<Integer> included, Set<Integer> excluded) {
		return overallModelCount;
	}

	@Override
	public Set<Integer> getVariables() {
		return new HashSet<>();
	}

}
