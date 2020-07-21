package ddnnfparsing;

import java.util.Map;

import org.prop4j.Node;

public class False extends Node {

	@Override
	public boolean getValue(Map<Object, Boolean> assignment) {
		return false;
	}

	@Override
	public boolean isConjunctiveNormalForm() {
		return false;
	}

	@Override
	public boolean isClausalNormalForm() {
		return false;
	}

	@Override
	protected Node eliminateNonCNFOperators(Node[] newChildren) {
		return null;
	}

	@Override
	public Node clone() {
		return new False();
	}

}
