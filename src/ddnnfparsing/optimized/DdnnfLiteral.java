package ddnnfparsing.optimized;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author Chico Sundermann
 *
 */
public class DdnnfLiteral extends DdnnfNode {

	int variable;
	
	public DdnnfLiteral(int variable) {
		super(BigInteger.ONE);
		this.variable = variable;
	}

	@Override
	public BigInteger computePartialConfiguration(Set<Integer> included, Set<Integer> excluded) {
		 if (included.contains(variable)) {
			 return BigInteger.ONE;
		 } else if (excluded.contains(variable)) {
			 return BigInteger.ZERO;
		 } else {
			 return null;
		 }
	}

	@Override
	public Set<Integer> getVariables() {
		Set<Integer> variables = new HashSet<>();
		variables.add(variable);
		return variables;
	}

}
