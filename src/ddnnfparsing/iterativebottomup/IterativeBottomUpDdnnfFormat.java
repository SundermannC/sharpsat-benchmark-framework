package ddnnfparsing.iterativebottomup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sat4j.minisat.core.Propagatable;

import ddnnfparsing.DDNNFParserUtils;

public class IterativeBottomUpDdnnfFormat {

	List<IterativeBUNode> nodes;
	
	IterativeBUNode root;
	
	// URS Variables
	Set<Integer> included;
	Set<Integer> excluded;
	BigInteger randomNumber;
	
	int numberOfVariables;
	
	int[] coreDeadIndicators;
	
	Set<Integer> cores;
	
	Set<Integer> deads;
	
	
	public void readDdnnfFile(String path) {
		nodes = new ArrayList<>();
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
			if (DDNNFParserUtils.isNegativeLiteral(split)) {
				int index =  DDNNFParserUtils.getNegativeLiteral(split);
				nodes.add(new IterativeNot(index));
			} else {
				nodes.add(new IterativeLiteral(DDNNFParserUtils.getPositiveLiteral(split)));
			}
		} else if (DDNNFParserUtils.isAnd(split)) {
			int[] childIndices = DDNNFParserUtils.getAndChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new IterativeTrue());
			} else {
				IterativeAnd node = new IterativeAnd();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);
			}
		} else if (DDNNFParserUtils.isOr(split)) {
			int[] childIndices = DDNNFParserUtils.getOrChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new IterativeFalse());
//			} else if (DDNNFParserUtils.isDecision(split)) {
//				IterativeDecision node = new IterativeDecision(Integer.valueOf(split[1]));
//				node.addLeftChild(nodes.get(childIndices[0]));
//				node.addRightChild(nodes.get(childIndices[1]));
//				nodes.add(node);
			} else {
				IterativeOr node = new IterativeOr();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);			
			}
		}
	}
	
	
	public void finish() {
		root = nodes.get(nodes.size() - 1);
	}
	
	public void readDdnnfFileAndSaveCoreDead(String path) {
		nodes = new ArrayList<>();
		try {
	        BufferedReader reader;
			reader = new BufferedReader(new FileReader(path));
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	handleLineWithCoreDead(line);
	        }
	        reader.close();
	        finishWithCoreDead();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	

	
	public void handleLineWithCoreDead(String line) {
		boolean foundEntry = false;
		String[] split = line.split(" ");
		
		if (!foundEntry && DDNNFParserUtils.isEntry(split)) {
			numberOfVariables = Integer.valueOf(split[3]);
			coreDeadIndicators = new int[numberOfVariables + 1];
			foundEntry = true;
		} else if (DDNNFParserUtils.isLiteral(split)) {
			if (DDNNFParserUtils.isNegativeLiteral(split)) {
				int index =  DDNNFParserUtils.getNegativeLiteral(split);
				nodes.add(new IterativeNot(index));
				if (coreDeadIndicators[index] == 0) {
					coreDeadIndicators[index] = -1;
				} else if (coreDeadIndicators[index] == 1) {
					coreDeadIndicators[index] = 2;
				}
			} else {
				int index = DDNNFParserUtils.getPositiveLiteral(split);
				nodes.add(new IterativeLiteral(index));
				if (coreDeadIndicators[index] == 0) {
					coreDeadIndicators[index] = 1;
				} else if (coreDeadIndicators[index] == -1) {
					coreDeadIndicators[index] = 2;
				}
			}
		} else if (DDNNFParserUtils.isAnd(split)) {
			int[] childIndices = DDNNFParserUtils.getAndChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new IterativeTrue());
			} else {
				IterativeAnd node = new IterativeAnd();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);
			}
		} else if (DDNNFParserUtils.isOr(split)) {
			int[] childIndices = DDNNFParserUtils.getOrChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new IterativeFalse());
			}
//			else if (DDNNFParserUtils.isDecision(split)) {
//				IterativeDecision node = new IterativeDecision(Integer.valueOf(split[1]));
//				node.addLeftChild(nodes.get(childIndices[0]));
//				node.addRightChild(nodes.get(childIndices[1]));
//				nodes.add(node);
//			} 
			else {
				IterativeOr node = new IterativeOr();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);			
			}
		}
	}

	public void finishWithCoreDead() {
		deads = new HashSet<>();
		cores = new HashSet<>();
		root = nodes.get(nodes.size() - 1);
		for (int i = 1; i < coreDeadIndicators.length; i++) {
			if (coreDeadIndicators[i] == -1) {
				deads.add(i);
			} else if (coreDeadIndicators[i] == 1) {
				cores.add(i);
			}
		}
		coreDeadIndicators = null;
	}
	

	
	public BigInteger getNumberOfSolutions() {
		return root.overallModelCount;
	}
	
	//-------------------------- Commonality -----------------------
	
	public List<BigInteger> getCommonalities() {
		List<BigInteger> commonalities = new ArrayList<>();
		for (int i = 1; i <= numberOfVariables; i++) {
			if (cores.contains(i)) {
				commonalities.add(root.overallModelCount);
			} else if (deads.contains(i)) {
				commonalities.add(BigInteger.ZERO);
			} else {
				for (IterativeBUNode node : nodes) {
					node.propagateCommonality(i); 
				}
				commonalities.add(root.tempModelCount);
			}
		}
		return commonalities;
	}
	
	
	
	
	//-------------------------- Uniform Random Sampling -----------------------
	
	
	public Set<Integer> performUrs(BigInteger randomNumber) {
		ursInit(randomNumber);
		for (int i = 1; i <= numberOfVariables; i++) {
			ursHandleNextVariable(i);
		}
		return ursFinish();
	}
	
	public void ursInit(BigInteger randomNumber) {
		included = new HashSet<>();
		excluded = new HashSet<>();
		this.randomNumber = randomNumber;
	}
	
	
	public void ursHandleNextVariable(int variableIndex) {
		if (cores.contains(variableIndex)) {
			included.add(variableIndex);
		} else if (deads.contains(variableIndex)) {
		// Skip feature
		} else {
			excluded.add(variableIndex);
			BigInteger result = getPartialConfigurationCount(included, excluded);
			if (result.compareTo(randomNumber) < 0) {
				randomNumber = randomNumber.subtract(result);
				included.add(variableIndex);
				excluded.remove(variableIndex);
			}
		}
	}
	
	public Set<Integer> ursFinish() {
		for (IterativeBUNode node : nodes) {
			node.resetCurrentModelCount();
		}
		return included;
	}
	
	public void ursSaveTempRessults() {
		for (IterativeBUNode node : nodes) {
			node.saveUnsureResult();
		}
	}
	
	public void resetTemps() {
		for (IterativeBUNode node : nodes) {
			node.resetCurrentModelCount();
		}
	}
	

	
	// Partial Configurations
	
	
	public BigInteger getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		for (IterativeBUNode node : nodes) {
			node.getPartialConfigurationCount(included, excluded);
		}
		return root.tempModelCount;
	}
	
}
