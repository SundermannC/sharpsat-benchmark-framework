package algorithms.groups.commonality;

import java.math.BigInteger;
import ddnnfparsing.DDNNFPropFormat;
import ddnnfparsing.SmartDDNNFFormat;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.FMUtils;
import utils.FileUtils;

import algorithms.basics.IComparableAlgorithm;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import java.lang.InterruptedException;
import java.lang.Override;
import java.lang.Runtime;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.lang.System;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import resultpackages.CompareSolverResultPackage;
import resultpackages.InstanceResult;
import resultpackages.PreciseAnalysisResultPackage;
import utils.BenchmarkUtils;
import utils.BinaryRunner;
import utils.DIMACSUtils;

public class DdnnfPropCommonality implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "DdnnfPropCommonality";

  public static final String ALGORITHM_GROUPID = "commonality";

  @Override
  public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file,
      List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, List<InstanceResult>> results = new HashMap<>();
    List<InstanceResult> resultPackage;
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    resultPackage = new ArrayList<>();
    for (IComparableSolver solver : solvers) {
      long startTime = System.nanoTime();
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
      long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
      runner.killCurrentProcess();
      solverResult = solver.getResult(binaryResult.stdout);
      resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
    }
    results.put("ddnnf", resultPackage);
    DDNNFPropFormat format = new DDNNFPropFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    HashMap<Integer,BigInteger> commonalities = format.computeCommonalities();
    return new CompareSolverResultPackage(solvers, results, ALGORITHM_ID);
  }

  @Override
  public Map<String, String> measureRuntime(BinaryRunner runner, String file,
      IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, String> results = new HashMap<>();
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    solverResult = solver.getResult(binaryResult.stdout);
    DDNNFPropFormat format = new DDNNFPropFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    HashMap<Integer,BigInteger> commonalities = format.computeCommonalities();
    results.put("commonalities", commonalities.toString());
    return results;
  }

  @Override
  public Map<String, String> measureRuntime(BinaryRunner runner, List<String> files,
      IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, String> results = new HashMap<>();
    for(String file : files) {
      Map<String, String> interimResult = measureRuntime(runner,file, solver, timeout, preprocessResult);
      results.putAll(interimResult);
    }
    return results;
  }

  @Override
  @SuppressWarnings("unused")
  public PreciseAnalysisResultPackage preciseAnalysis(BinaryRunner runner, String file,
      IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    PreciseAnalysisResultPackage results = new PreciseAnalysisResultPackage(file);
    Runtime runtime = Runtime.getRuntime();
    System.gc();
    long startMemory = runtime.totalMemory() - runtime.freeMemory();
    long maxMemory = 0;
    long tempMemory;
    String maxMemorySource = "";
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    results.startClock("solver");
    binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    results.stopClock("solver");
    solverResult = solver.getResult(binaryResult.stdout);
    DDNNFPropFormat format = new DDNNFPropFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    HashMap<Integer,BigInteger> commonalities = format.computeCommonalities();
    results.maxMemory = maxMemory;
    results.maxMemorySource = maxMemorySource;
    return results;
  }

  @Override
  public String getAlgorithmId() {
    return ALGORITHM_ID;
  }

  @Override
  public String getAlgorithmGroupId() {
    return ALGORITHM_GROUPID;
  }

}