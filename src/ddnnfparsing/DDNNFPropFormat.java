package ddnnfparsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.prop4j.Or;

import comparablesolver.ComparableSharpSAT;
import comparablesolver.IComparableSolver;
import ddnnfparsing.bottomup.BottomupDdnnfFormat;
import ddnnfparsing.optimized.OptimizedDdnnfFormat;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkUtils;
import utils.BinaryRunner;
import utils.FMUtils;

public class DDNNFPropFormat implements IDDNNFFormat {

	
	// Big overhead lets take a look at this
	private List<Node> nodes;
	
	private Node root;
	
	int numberOfFeatures = 0;
	
	Set<Integer> includedPartialConfiguration;
	
	Set<Integer> excludedPartialConfiguration;
	
	
	private class IntermediateResult {
		public byte[] definedFeatures;
		public long intermediateCount;
	}
	
	public DDNNFPropFormat() {
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
			int[] childIndices = DDNNFParserUtils.getAndChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new True());
			} else {
				nodes.add(new And(getNodesByIndices(childIndices)));
			}
		} else if (DDNNFParserUtils.isOr(split)) {
			int[] childIndices = DDNNFParserUtils.getOrChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new False());
			} else {
				nodes.add(new Or(getNodesByIndices(DDNNFParserUtils.getOrChildIndices(split))));				
			}
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
		root = nodes.get(nodes.size() - 1);
		nodes = new ArrayList<>();
	}
	
	
	@Override
	public long countNumberOfSolutions() {
		return recursiveCount(root).intermediateCount;
	}
	
	@Override
	public HashMap<Integer, BigInteger> computeCommonalities() {
		return recursiveCommonalities(root); 
	}
	
	public BigInteger computePartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		includedPartialConfiguration = included;
		excludedPartialConfiguration = excluded;
		return recursivePartialConfigurations(root);
	}
	
	
	// MODEL COUNTING
	
	// TODO: update this
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
	
	
	// COMMONALITIES
	
	
	/**
	 * Computes the commonalities and the overall model count given the root of prop4j ddnnf 
	 * @param node
	 * @return List of commonalities: Index 0 is overall model count; Index i > 0 to variable i
	 */
	private HashMap<Integer, BigInteger> recursiveCommonalities(Node node) {
		 if (node instanceof And) {
			final Node[] children = node.getChildren();
			HashMap<Integer, BigInteger> commonalities = recursiveCommonalities(children[0]);
			for (int i = 1; i < children.length; i++) {
				mergeNextAndChild(commonalities, recursiveCommonalities(children[i]));
			}
			return commonalities;
		 } else if (node instanceof Or) {
			final Node[] children = node.getChildren();
			HashMap<Integer, BigInteger> commonalities = recursiveCommonalities(children[0]);
			for (int i = 1; i < children.length; i++) {
				mergeNextOrChild(commonalities, recursiveCommonalities(children[i]));
			}
			return commonalities;
		 } else if (node instanceof Literal || node instanceof Not) {
			 HashMap<Integer, BigInteger> commonalities = new HashMap<>();
			 int literalIndex = Integer.valueOf((String) node.getLiterals().get(0).var);
			// in any case one solution results from a leaf
			 commonalities.put(0, BigInteger.ONE);
			 // the solution contains the variable
			 if (node instanceof Literal) {
				commonalities.put(literalIndex, BigInteger.ONE);
			 } else {
				 commonalities.put(literalIndex, BigInteger.ZERO);
			 }
			 return commonalities;
		 } else if(node instanceof True) {
			 HashMap<Integer, BigInteger> commonalities = new HashMap<>();
			 commonalities.put(0, BigInteger.ONE);
			 return commonalities;
		 } else if (node instanceof False) {
			 HashMap<Integer, BigInteger> commonalities = new HashMap<>();
			 commonalities.put(0, BigInteger.ZERO);
			 return commonalities;
		 }
		 return null;

	}
	
	private void mergeNextOrChild(HashMap<Integer, BigInteger> result, HashMap<Integer, BigInteger> toMerge) {
		for (Integer index : result.keySet()) {
			result.put(index, result.get(index).add(toMerge.get(index)));
		}
	}
	
	private void mergeNextAndChild(HashMap<Integer, BigInteger> result, HashMap<Integer, BigInteger> toMerge) {
		BigInteger overallCount = new BigInteger(result.get(0).toString());
		for (Integer index : result.keySet()) {
			result.put(index, result.get(index).multiply(toMerge.get(0)));
		}
		for (Integer index: toMerge.keySet()) {
			result.put(index, overallCount.multiply(toMerge.get(index)));
		}
	}
	
	
	private BigInteger recursivePartialConfigurations(Node node) {
		 if (node instanceof And) {
			final Node[] children = node.getChildren();
			boolean relevantFeatureInSubtree = false;
			BigInteger partialConfigurationCount = BigInteger.ONE;
			for (int i = 0; i < children.length; i++) {
				partialConfigurationCount = partialConfigurationCount.multiply(recursivePartialConfigurations(children[i]));
			}
			return partialConfigurationCount;
		 } else if (node instanceof Or) {
			final Node[] children = node.getChildren();
			BigInteger count = recursivePartialConfigurations(children[0]);
			// Subtree contains no features of the partial configuration
			for (int i = 1; i <children.length; i++) {
				count = count.add(recursivePartialConfigurations(children[i]));
			}
			return count;
		 } else if (node instanceof Literal || node instanceof Not) {
			 int literalIndex = Integer.valueOf((String) node.getLiterals().get(0).var);

			// in any case one solution results from a leaf
			 // the solution contains the variable
			 if (node instanceof Literal) {
				 if (includedPartialConfiguration.contains(literalIndex)) {
					 return BigInteger.ONE;
				 } else if (excludedPartialConfiguration.contains(literalIndex)) {
					 return BigInteger.ZERO;
				 } 
			 } else {
				 if (includedPartialConfiguration.contains(literalIndex)) {
					 return  BigInteger.ZERO;
				 } else if (excludedPartialConfiguration.contains(literalIndex)) {
					 return BigInteger.ONE;
				 } 
			 }
			 return BigInteger.ONE;
		 } else if(node instanceof True) {
			 return BigInteger.ONE;
		 } else if (node instanceof False) {
			 return BigInteger.ZERO;
		 }
		 System.out.println("Ooops! Illegal node");
		 return null;
	}
	

	
	
	
	

}
