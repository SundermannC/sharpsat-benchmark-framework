package ddnnfparsing.optimized;

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

import ddnnfparsing.DDNNFParserUtils;
import utils.BenchmarkUtils;
import utils.FMUtils;

public class OptimizedDdnnfFormat {
	
	


	protected class IntermediateResult {
		public boolean changed;
		public BigInteger count;
	}
	
	
	private DdnnfNode root;
	
	/**
	 * This is cleared after the building process is finished
	 */
	private List<DdnnfNode> nodes;
	
	
	public BigInteger getModelCount() {
		return root.overallModelCount;
	}
	
	public HashMap<Integer,BigInteger> getCommonalities() {
		HashMap<Integer, BigInteger> commonalities = new HashMap<>();
		for (Integer var : root.getVariables()) {
			Set<Integer> included = new HashSet<>();
			included.add(var);
			commonalities.put(var, getPartialConfigurationCount(included, new HashSet<Integer>()));
		}
		return commonalities;
	}
	
	public BigInteger getPartialConfigurationCount(Set<Integer> included, Set<Integer> excluded) {
		return root.computePartialConfiguration(included, excluded);
	}
	
	
	
	public void readDdnnfFile(String path) {
		nodes = new ArrayList<DdnnfNode>();
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
				nodes.add(new DdnnfNot(DDNNFParserUtils.getNegativeLiteral(split)));
			} else {
				nodes.add(new DdnnfLiteral(DDNNFParserUtils.getPositiveLiteral(split)));
			}
		} else if (DDNNFParserUtils.isAnd(split)) {
			int[] childIndices = DDNNFParserUtils.getAndChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new DdnnfTrue());
			} else {
				DdnnfAnd node = new DdnnfAnd();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);
			}
		} else if (DDNNFParserUtils.isOr(split)) {
			int[] childIndices = DDNNFParserUtils.getOrChildIndices(split);
			if (childIndices.length == 0) {
				nodes.add(new DdnnfFalse());
			} else {
				DdnnfOr node = new DdnnfOr();
				for (int childIndex : childIndices) {
					node.addChild(nodes.get(childIndex));
				}
				nodes.add(node);			
			}
		}
	}
	
	public void finish() {
		root = nodes.get(nodes.size() - 1);
		nodes = null;
	}
	
}
