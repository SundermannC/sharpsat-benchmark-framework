package ddnnfparsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;

public class UnsmoothDDNNFFormat implements IDDNNFFormat {

	
	// Big overhead lets take a look at this
	private List<Node> nodes;
	
	private Node root;
	
	int numberOfFeatures = 0;
	
	private class IntermediateResult {
		public byte[] definedFeatures;
		public long intermediateCount;
	}
	
	public UnsmoothDDNNFFormat() {
		nodes = new ArrayList<>();
	}
	
	public void readDdnnfFile(String path) {
		try {
	        BufferedReader reader;
			reader = new BufferedReader(new FileReader(path));
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	handleLine(line);
	        }
	        reader.close();
	        finish();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	@Override
	public void handleLine(String line) {
		String[] split = line.split(" ");
		
		if (DDNNFParserUtils.isEntry(split)) {
			numberOfFeatures = Integer.valueOf(split[3]);
		}
		if (DDNNFParserUtils.isLiteral(split)) {
			if (DDNNFParserUtils.isNegativeLiteral(split)) {
				nodes.add(new Not(DDNNFParserUtils.getNegativeLiteralName(split)));
			} else {
				nodes.add(new Literal(DDNNFParserUtils.getPositiveLiteralName(split)));
			}
		} else if (DDNNFParserUtils.isAnd(split)) {
			nodes.add(new And(getNodesByIndices(DDNNFParserUtils.getAndChildIndices(split))));
		} else if (DDNNFParserUtils.isOr(split)) {
			nodes.add(new Or(getNodesByIndices(DDNNFParserUtils.getOrChildIndices(split))));
		}
	}
	
	private Node[] getNodesByIndices(int[] indices) {
		Node[] nodeArray = new Node[indices.length];
		for (int i = 0; i < indices.length; i++) {
			nodeArray[i] = nodes.get(indices[i]);
		}
		return nodeArray;
	}

	@Override
	public void finish() {
		root = nodes.get(nodes.size() - 1 );
	}
	
	
	@Override
	public long countNumberOfSolutions() {
		return recursiveCount(root).intermediateCount;
	}
	
	@Override
	public HashMap<Integer, BigInteger> computeCommonalities() {
		return null; // TODO: Maybe but this is probably to delete anyways
	}
	
	/**
	 * Computes the commonalities and the overall model count given the root of prop4j ddnnf 
	 * @param node
	 * @return List of commonalities: Index 0 is overall model count; Index i > 0 to variable i
	 */
	private BigInteger[] recursiveCommonalities(Node node) {
		 BigInteger[] tempCommonalities = new BigInteger[numberOfFeatures + 1];
		 Node[] children = node.getChildren();
		 if (node instanceof And) {
			 BigInteger[][] childrenCommonalities = new BigInteger[children.length][numberOfFeatures + 1];
			 // Recurse for children
			 for (int i = 0; i < children.length; i++) {
				 childrenCommonalities[i] = recursiveCommonalities(children[i]);
			 }
			 
			 for (int i = 0; i < numberOfFeatures + 1; i++) {
				 boolean isIncludedOnce = false;
				 BigInteger[] factors = new BigInteger[children.length];
				 for (int j = 0; j < childrenCommonalities.length; j++) {
					 // For every child that does not include the variable add OV
					 if (childrenCommonalities[j][i] == null) {
						 factors[j] = childrenCommonalities[j][0];
					 } else {
						 factors[j] = childrenCommonalities[j][i];
						 isIncludedOnce = true;
					 }
				 }
				 if (isIncludedOnce) {
					 BigInteger commonality = BigInteger.valueOf(1);
					 for (BigInteger factor : factors) {
						 commonality = commonality.multiply(factor);
					 }
					 tempCommonalities[i] = commonality;
				 }
			 }
		 } else if (node instanceof Or) {
			 BigInteger[][] childrenCommonalities = new BigInteger[children.length][numberOfFeatures + 1];
			 // Recurse for Children
			 for (int i = 0; i < children.length; i++) {
				 childrenCommonalities[i] = recursiveCommonalities(children[i]);
			 }
			 
			 // initialize factors
			 BigInteger[] factors = new BigInteger[children.length];
			 for (int i = 0; i < children.length; i++) {
				 factors[i] = BigInteger.valueOf(1);
			 }
			 
			 // compute the factors that results from freely assignable variables
			 for (int i = 0; i < numberOfFeatures + 1; i++) {
					boolean featureContained = false;
					boolean[] hasFeature = new boolean[children.length];
					for (int j = 0; j < children.length; j ++) {
						if (childrenCommonalities[j][i] != null) {
							hasFeature[j] = true;
							featureContained = true;
						} else {
							hasFeature[j] = false;
						}
					}
					if (featureContained) {
						for (int j = 0; j < children.length; j++) {
							if (!hasFeature[j]) {
								factors[j] = factors[j].multiply(BigInteger.valueOf(2));
							}
						}
						featureContained = false;
					}
			 }
			 
			 // Compute overall model count
			 BigInteger tempSum = BigInteger.valueOf(0);
			 for (int i = 0; i < children.length; i++) {
				 childrenCommonalities[i][0] = childrenCommonalities[i][0].multiply(factors[i]);
				 tempSum = tempSum.add(childrenCommonalities[i][0]);
			 }
			 tempCommonalities[0] = tempSum;
			 
			 // compute commonalities
			 BigInteger[] summands = new BigInteger[children.length];
			 for (int i = 1; i < numberOfFeatures + 1; i++) {
				 boolean isIncludedOnce = false;
				 for (int j = 0; j < childrenCommonalities.length; j++) {
					 if (childrenCommonalities[j][i] == null) {
						// If the variable does not explicitly appear in this child; it appears in half of the overall solutions
						 // should still not be a float in any case; was at least once multiplied with 2
						 summands[j] = childrenCommonalities[j][0].divide(BigInteger.valueOf(2));
					 } else {
						 summands[j] = childrenCommonalities[j][i].multiply(factors[j]);
						 isIncludedOnce = true;
					 }
				 }
				 // if none of the children explicitly contains the variable, commonality of this feature stays null
				 if (isIncludedOnce) {
					 BigInteger commonality = BigInteger.valueOf(0);
					 for (BigInteger summand : summands) {
						 commonality = commonality.add(summand);
					 }
					 tempCommonalities[i] = commonality;
				 }
			 }
			 
		 } else if (node instanceof Literal || node instanceof Not) {
			 int literalIndex = Integer.valueOf((String) node.getLiterals().get(0).var);
			 // in any case one solution results from a leaf
			 tempCommonalities[0] = BigInteger.valueOf(1);
			 // the solution contains the variable
			 if (node instanceof Literal) {
				tempCommonalities[literalIndex] = BigInteger.valueOf(1);
				// the solution does not contains the variable
			 } else if (node instanceof Not) {
				 tempCommonalities[literalIndex] = BigInteger.valueOf(0);
			 }
		 }
		 return tempCommonalities;
	}
	
	public IntermediateResult recursiveCount(Node node) {
		IntermediateResult result = new IntermediateResult();
		long tempResult;
		byte[] tempDefinedFeatures = new byte[numberOfFeatures];
		// Product of all child values
		if (node instanceof And) {
			tempResult = 1;
			Node[] children = node.getChildren();
			for (int i = 0; i < children.length; i++) {
				IntermediateResult interm = recursiveCount(children[i]);
				tempResult = tempResult * interm.intermediateCount;
				for (int j = 0; j < numberOfFeatures; j++) {
					if (interm.definedFeatures[j] == 1) {
						tempDefinedFeatures[j] = 1;
					}
				}

 			}
		// Sum of all child values
		} else if (node instanceof Or) {
			tempResult = 0;
			Node[] children = node.getChildren();
			IntermediateResult[] childResults = new IntermediateResult[children.length];
			for (int i = 0; i < children.length; i++) {
				childResults[i] =  recursiveCount(children[i]);
			}
			int[] multiplicators = new int[childResults.length];
			for (int i = 0; i < multiplicators.length; i++) {
				multiplicators[i] = 1;
			}
			// compute the factors that results from freely assignable variables
			for (int i = 0; i < numberOfFeatures;i++) {
				boolean featureContained = false;
				boolean[] hasFeature = new boolean[childResults.length];
				for (int j = 0; j < childResults.length; j ++) {
					if (childResults[j].definedFeatures[i] != 0) {
						hasFeature[j] = true;
						featureContained = true;
					} else {
						hasFeature[j] = false;
					}
				}
				if (featureContained) {
					tempDefinedFeatures[i] = 1;
					featureContained = false;
					for (int j = 0; j < childResults.length; j ++) {
						if (!hasFeature[j]) {
							multiplicators[j] = multiplicators[j] * 2;
						}
					}
				}
			}
			for (int i = 0; i < childResults.length; i++) {
				tempResult += childResults[i].intermediateCount * multiplicators[i];
			}
			
		} else if (node instanceof Literal || node instanceof Not) {
			tempResult = 1;
			int literalIndex = Integer.valueOf((String) node.getLiterals().get(0).var);
			tempDefinedFeatures[literalIndex - 1] = 1;
		} else {
			tempResult = -1;
		}
		result.intermediateCount = tempResult;
		result.definedFeatures = tempDefinedFeatures;
		
		return result;
	}
	
	
	
	

}
