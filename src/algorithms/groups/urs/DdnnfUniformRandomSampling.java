package algorithms.groups.urs;

import java.math.BigInteger;
import java.util.Set;
import algorithms.groups.urs.URSPreprocessorResult;
import ddnnfparsing.bottomup.BottomupDdnnfFormat;
import ddnnfparsing.iterativebottomup.IterativeBottomUpDdnnfFormat;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
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

public class DdnnfUniformRandomSampling implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "DdnnfUniformRandomSampling";

  public static final String ALGORITHM_GROUPID = "urs";

  @Override
  public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file,
      List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, List<InstanceResult>> results = new HashMap<>();
    List<InstanceResult> resultPackage;
    URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
    BinaryResult binaryResult;
    SolverResult solverResult;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    resultPackage = new ArrayList<>();
    for (IComparableSolver solver : solvers) {
      long startTime = System.nanoTime();
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
      long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
      runner.killProcessesByUserAndName(solver.getBinaryName());
      solverResult = solver.getResult(binaryResult.stdout);
      resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
    }
    results.put(modelName, resultPackage);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFileAndSaveCoreDead(BenchmarkConstants.DDNNF_TEMP_PATH);
    for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
      Set<Integer> config = format.performUrs(randomNumber);
      String resultString = "";
      for (Integer included : config)  {
        resultString += included + ",";
      }
    }
    return new CompareSolverResultPackage(solvers, results, ALGORITHM_ID);
  }

  @Override
  public Map<String, String> measureRuntime(BinaryRunner runner, String file,
      IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, String> results = new HashMap<>();
    URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
    BinaryResult binaryResult;
    SolverResult solverResult;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    solverResult = solver.getResult(binaryResult.stdout);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFileAndSaveCoreDead(BenchmarkConstants.DDNNF_TEMP_PATH);
    for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
      Set<Integer> config = format.performUrs(randomNumber);
      String resultString = "";
      for (Integer included : config)  {
        resultString += included + ",";
      }
      results.put(modelName + randomNumber.toString(), resultString);
    }
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
    URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
    BinaryResult binaryResult;
    SolverResult solverResult;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    results.startClock("solver");
    binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    results.stopClock("solver");
    solverResult = solver.getResult(binaryResult.stdout);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFileAndSaveCoreDead(BenchmarkConstants.DDNNF_TEMP_PATH);
    for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
      Set<Integer> config = format.performUrs(randomNumber);
      String resultString = "";
      for (Integer included : config)  {
        resultString += included + ",";
      }
    }
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