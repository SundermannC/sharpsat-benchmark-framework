package main;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import comparablesolver.ComparableCountAntom;
import comparablesolver.ComparableDsharp;
import comparablesolver.IComparableSolver;
import ddnnfparsing.iterativebottomup.IterativeBottomUpDdnnfFormat;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.BenchmarkUtils;
import utils.BinaryRunner;
import utils.DIMACSUtils;
import utils.FMUtils;

public class DdnnfTests {
	public static void main(String[] args) throws InterruptedException {
		FMUtils.installLibraries();
//		String testDimacsPath = "test_dimacs/simpletest.dimacs.nnf";
		//String testDimacsPath = "test_dimacs/easy_test/busybox.dimacs.nnf";
//		String testDimacsPath = "test_dimacs/medium_test/aaed2000.dimacs.nnf);
//		String testDimacsPath = BenchmarkConstants.DDNNF_TEMP_PATH;
		String testDimacsPath = "/home/chico/Documents/Programming/Eclipse/eclipse4.8.0committers-featureide3.5.5-linux64/eclipse4.9.0committers-featureide3.5.5-linux64/masterarbeit/Car/model.xml";
		
		String testModel = testDimacsPath;
		IFeatureModel model = FMUtils.readFeatureModel(testModel);
	    FeatureModelFormula formula = new FeatureModelFormula(model);
	    CNF cnf = formula.getCNF();   
	    
	    Map<String, BigInteger> commonalitiesDpll = new HashMap<>();
	    for (IFeature feature : model.getFeatures()) {
	    	CNF clone = cnf.clone();
	    	clone.addClause(new LiteralSet(clone.getVariables().getVariable(feature.getName())));
	    	DIMACSUtils.createTemporaryDimacs(clone);
	    	IComparableSolver solver = new ComparableCountAntom(8000);
	    	BinaryResult result = solver.executeSolver(new BinaryRunner(5), "temp.dimacs", 5);
	    	SolverResult sresult = solver.getResult(result.stdout);
	    	commonalitiesDpll.put(feature.getName(), sresult.result);
	    }
	    
	    
	    System.out.println(commonalitiesDpll.toString());
	    BigInteger sum = BigInteger.ZERO;
	    for (String key: commonalitiesDpll.keySet()) {
	    	sum = sum.add(commonalitiesDpll.get(key));
	    }
	    BigDecimal homogeneity = new BigDecimal(sum).divide(BigDecimal.valueOf(commonalitiesDpll.size())).divide(new BigDecimal(BigInteger.valueOf(42)));
	    
	    System.out.println(homogeneity);
	    
//		IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
//		
//		long startNano = System.nanoTime();
//		format.readDdnnfFileAndSaveCoreDead(testDimacsPath);
//		long endNano = System.nanoTime();
//		long durationReading = BenchmarkUtils.getDurationNano(startNano, endNano);
//		System.out.println("Time to parse: " + BenchmarkUtils.translateToTimeUnit(durationReading, TimeUnit.SECONDS));
//		System.out.println("Number of solutions:"  + format.getNumberOfSolutions().toString());
//		
//		
//		startNano = System.nanoTime();
//		List<BigInteger> commonalities = format.getCommonalities();
//		endNano = System.nanoTime();
//		durationReading = BenchmarkUtils.getDurationNano(startNano, endNano);
//		System.out.println("Time to compute: " + BenchmarkUtils.translateToTimeUnit(durationReading, TimeUnit.SECONDS));
		
		
//		startNano = System.nanoTime();
//		BigInteger partialResult = format.getPartialConfigurationCount(included, excluded);
//		endNano = System.nanoTime();
//		durationReading = BenchmarkUtils.getDurationNano(startNano, endNano);
//		System.out.println("Time to compute: " + BenchmarkUtils.translateToTimeUnit(durationReading, TimeUnit.SECONDS));
//		System.out.println("Number of partial configuration solutions:" +  partialResult.toString());
		
//		startNano = System.nanoTime();
//		Set<Integer> config = format.performUrs(format.getNumberOfSolutions().divide(BigInteger.TEN));
//		endNano = System.nanoTime();
//		durationReading = BenchmarkUtils.getDurationNano(startNano, endNano);
//		System.out.println("Time to compute: " + BenchmarkUtils.translateToTimeUnit(durationReading, TimeUnit.SECONDS));
		
		

	}
}
