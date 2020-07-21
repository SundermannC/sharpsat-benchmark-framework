package ddnnfparsing.bottomup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ddnnfparsing.DDNNFParserUtils;

public class BottomupDdnnfFormat {
	
	Map<Integer, BottomupDdnnfNode> positiveLiterals;
	
	Map<Integer, BottomupDdnnfNode> negativeLiterals;
	
	List<BottomupDdnnfNode> nodes;
	
	BottomupDdnnfNode root;
	
	// Uniform random sampling variables
	
	List<Integer> currentConfig;
	
	BigInteger randomNumber;
	
	
	
	// -------------- Parse d-DNNF --------------
	
	public void readDdnnfFile(String path) {
		nodes = new ArrayList<>();
		positiveLiterals = new HashMap<>();
		negativeLiterals = new HashMap<>();
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
	
	
	public void handleLine(String line) {
		String[] split = line.split(" ");
		
		if (DDNNFParserUtils.isLiteral(split)) {
			BottomupDdnnfNode literal = new BottomupLiteral();
			if (DDNNFParserUtils.isNegativeLiteral(split)) {
				negativeLiterals.put(DDNNFParserUtils.getNegativeLiteral(split), literal);
			} else {
				positiveLiterals.put(DDNNFParserUtils.getPositiveLiteral(split), literal);
			}
			nodes.add(literal);
		} else if (DDNNFParserUtils.isAnd(split)) {
			int[] childIndices = DDNNFParserUtils.getAndChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new BottomupTrue());
			} else {
				BottomupAnd node = new BottomupAnd();
				for (int childIndex : childIndices) {
					BottomupDdnnfNode child = nodes.get(childIndex);
					child.addParent(node);
					node.addChild(child);
				}
				nodes.add(node);
			}
		} else if (DDNNFParserUtils.isOr(split)) {
			int[] childIndices = DDNNFParserUtils.getOrChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new BottomupFalse());
			} else {
				BottomupOr node = new BottomupOr();
				for (int childIndex : childIndices) {
					BottomupDdnnfNode child = nodes.get(childIndex);
					child.addParent(node);
					node.addChild(child);
				}
				nodes.add(node);			
			}
		}
	}
	
	public void finish() {
		for (BottomupDdnnfNode node : nodes) {
			node.resetTemps();
		}
		root = nodes.get(nodes.size() - 1);
	}
	
	//-------------------------- Model Counting --------------------------
	
	public BigInteger getNumberOfSolutions() {
		return root.overallModelCount;
	}
	
	public BigInteger getCurrentNumberOfSolutions() {
		return root.tempModelCount;
	}
	
	//-------------------------- Uniform Random Sampling -----------------------
	
	
	public void ursInit(BigInteger randomNumber) {
		currentConfig = new ArrayList<>();
		this.randomNumber = randomNumber;
	}
	
	
	public void ursHandleNextVariable(Integer variableIndex) {	
		if (positiveLiterals.containsKey(variableIndex)) {
			if (negativeLiterals.containsKey(variableIndex)) {
				positiveLiterals.get(variableIndex).propagateUnsureChange(null, null);
				if (root.unsureTempModelCount.compareTo(randomNumber) < 0) {
					negativeLiterals.get(variableIndex).propagateChange(null, null);
					randomNumber = randomNumber.subtract(root.tempModelCount);
					currentConfig.add(variableIndex);
				} else {
					positiveLiterals.get(variableIndex).saveUnsureResults();
				}
			} else {
				// If only positives literals appear the feature is core
				currentConfig.add(variableIndex);
			}
		} else if (negativeLiterals.containsKey(variableIndex)) {
			// If only negative literals appeaar the feature is dead			
			return;
		}

	}
	
	public List<Integer> ursFinish() {
		for (BottomupDdnnfNode node : nodes) {
			node.resetTemps();
		}
		return currentConfig;
	}
	
	
	
	// -------------------------- Partial Configuration -------------------------- 
	
	public BigInteger getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		for (Integer includedVar : included) {
			if (negativeLiterals.containsKey(includedVar)) {
				if (!positiveLiterals.containsKey(includedVar)) {
					return BigInteger.ZERO;
				} else {
					negativeLiterals.get(includedVar).propagateChange(null, null);					
				}
			} 
		}
		for (Integer excludedVar : excluded) {
			if (positiveLiterals.containsKey(excludedVar)) {
				positiveLiterals.get(excludedVar).propagateChange(null, null);
			}
		}
		
		return root.tempModelCount;
	}
	
	
}
