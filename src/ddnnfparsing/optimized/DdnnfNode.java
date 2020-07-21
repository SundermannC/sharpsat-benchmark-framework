package ddnnfparsing.optimized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DdnnfNode {
	
	
	protected BigInteger overallModelCount;
	
	protected List<DdnnfNode> children;
	
	public DdnnfNode(BigInteger overallModelCount) {
		this.overallModelCount = overallModelCount;
		children = new ArrayList<DdnnfNode>();
	}
	
	public void addChild(DdnnfNode child) {
		children.add(child);
	}
	
	public List<DdnnfNode> getChildren() {
		return children;
	}
	
	/**
	 * Computes the number of remaining valid configurations induced by the subtree of the node
	 * By the recursion logic this should only be called by nodes that contain one of the variables
	 * @param included features included in the partial configuration
	 * @param excluded features excluded from the partial configuration
	 * @return number of remaining valid configurations for the partial configuration induced by included and excluded
	 */
	public abstract BigInteger computePartialConfiguration(Set<Integer> included, Set<Integer> excluded);

	public abstract Set<Integer> getVariables();
}
