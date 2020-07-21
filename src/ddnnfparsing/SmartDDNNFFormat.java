package ddnnfparsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import comparablesolver.ComparableSharpSAT;
import comparablesolver.IComparableSolver;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;
import utils.FMUtils;
import utils.FileUtils;

public class SmartDDNNFFormat implements IDDNNFFormat {
	
	public static void main(String[] args) throws InterruptedException {
		FMUtils.installLibraries();
		
		List<File> paths = FileUtils.getFilesInDirectoryAndSubdirectories("test_dimacs/simpletest_commonalities");
		IComparableSolver solver = new ComparableSharpSAT(8000);
		for (File file : paths) {
			BinaryResult result = solver.executeSolver(new BinaryRunner(5), file.getAbsolutePath(), 5);
			SolverResult sresult = solver.getResult(result.stdout);
			System.out.println(file.getAbsolutePath() + ": " + sresult.result.toString());
		}
		
		DDNNFPropFormat propformat = new DDNNFPropFormat();
		propformat.readDdnnfFile("d4.nndf");
		HashMap<Integer,BigInteger> propsolutions = propformat.computeCommonalities();
		System.out.println("-----------Prop Format results-----------");
		System.out.println(propsolutions.toString());
		
		
		
		SmartDDNNFFormat format = new SmartDDNNFFormat();
		System.out.println("-----------sMaRt Format results-----------");
		HashMap<Integer, BigInteger> solutions = format.computeCommonalitiesDirectly("simpletest.dimacs.nnf");
		System.out.println(solutions.toString());
	}

	
	List<HashMap<Integer, BigInteger>> commonalitiesMaps;
	
	int numberOfFeatures;
	
	int currentIndex = 0;


	@Override
	public void handleLine(String line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public long countNumberOfSolutions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HashMap<Integer, BigInteger> computeCommonalities() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public BigInteger computeSolutionsDirectly() {
		return null;
	}
	
	public HashMap<Integer,BigInteger> computeCommonalitiesDirectly(String path) {
		commonalitiesMaps = new ArrayList<>();
		try {
	        BufferedReader reader;
			reader = new BufferedReader(new FileReader(path));
	        String line;
	        while ((line = reader.readLine()) != null) {
	        	handleLineDirectly(line);
	        }
	        reader.close();
	        finish();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return commonalitiesMaps.get(commonalitiesMaps.size() - 1);		
	}
	
	public void handleLineDirectly(String line) {
		String[] split = line.split(" ");
		if (DDNNFParserUtils.isLiteral(split)) {
			if (DDNNFParserUtils.isNegativeLiteral(split)) {
				commonalitiesMaps.add(parseLiteral(DDNNFParserUtils.getNegativeLiteral(split), false));
			} else {
				commonalitiesMaps.add(parseLiteral(DDNNFParserUtils.getPositiveLiteral(split), true));
			}
		} else if (DDNNFParserUtils.isAnd(split)) {
			commonalitiesMaps.add(mergeAndChildren(DDNNFParserUtils.getAndChildIndices(split)));
		} else if (DDNNFParserUtils.isOr(split)) {
			commonalitiesMaps.add(mergeOrChildren(DDNNFParserUtils.getOrChildIndices(split)));
		}
	}
	
	public HashMap<Integer, BigInteger> parseLiteral(Integer variableIndex, boolean positive) {
		HashMap<Integer, BigInteger> result = new HashMap<>();
		result.put(variableIndex, positive ? BigInteger.ONE : BigInteger.ZERO);
		result.put(0, BigInteger.ONE);
		return result;
	}
	
	
	
	public HashMap<Integer, BigInteger> mergeOrChildren(int[] childIndices) {
		HashMap<Integer, BigInteger> result = new HashMap<>();
		for (Integer variableIndex : commonalitiesMaps.get(childIndices[0]).keySet()) {
			BigInteger commonality = BigInteger.ZERO;
			for (int childIndex : childIndices) {
				commonality = commonality.add(commonalitiesMaps.get(childIndex).get(variableIndex));
			}
			result.put(variableIndex, commonality);
		}
		return result;
	}
	
	
	public HashMap<Integer, BigInteger> mergeAndChildren(int[] childIndices) {
		HashMap<Integer, BigInteger> result = new HashMap<>();
		for (int childIndex = 0; childIndex < childIndices.length; childIndex++) {
			for (Integer variableIndex : commonalitiesMaps.get(childIndices[childIndex]).keySet()) {
				BigInteger commonality = commonalitiesMaps.get(childIndices[childIndex]).get(variableIndex);
				for (int otherChildIndex = 0; otherChildIndex < childIndices.length; otherChildIndex++) {
					if (otherChildIndex == childIndex) {
						continue;
					}
					commonality = commonality.multiply(commonalitiesMaps.get(otherChildIndex).get(0));
				}
				result.put(variableIndex, commonality);
			}
		}
		return result;
	}
	
	
	
	
	
	
	

}
