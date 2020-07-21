package algorithms.groups.commonality;

import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
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
import java.math.BigInteger;
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

public class NaiveCommonality implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "NaiveCommonality";

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
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
      runner.killCurrentProcess();
      solverResult = solver.getResult(binaryResult.stdout);
      resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
    }
    results.put(modelName, resultPackage);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    for (IFeature feat : model.getFeatures())  {
      CNF temp = cnf.clone();
      String featName = feat.getName();
      int varIndex = temp.getVariables().getVariable(featName);
      temp.addClause(new LiteralSet(varIndex));
      DIMACSUtils.createTemporaryDimacs(temp);
      resultPackage = new ArrayList<>();
      for (IComparableSolver solver : solvers) {
        long startTime = System.nanoTime();
        binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
        long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
        runner.killCurrentProcess();
        solverResult = solver.getResult(binaryResult.stdout);
        resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
      }
      results.put(featName, resultPackage);
    }
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
    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
    solverResult = solver.getResult(binaryResult.stdout);
    results.put(modelName, solverResult.result.toString());
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    List<BigInteger> commonalities = new ArrayList<>();
    
    for (int i = 0; i < cnf.getVariables().size(); i++)  {
      CNF temp = cnf.clone();
      temp.addClause(new LiteralSet(i));
      DIMACSUtils.createTemporaryDimacs(temp);
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      solverResult = solver.getResult(binaryResult.stdout);
      //results.put(featName, solverResult.result.toString());
      commonalities.add(solverResult.result);
    }
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
    binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
    results.stopClock("solver");
    solverResult = solver.getResult(binaryResult.stdout);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    for (IFeature feat : model.getFeatures())  {
      results.startClock("ChangeFormula");
      CNF temp = cnf.clone();
      String featName = feat.getName();
      int varIndex = temp.getVariables().getVariable(featName);
      temp.addClause(new LiteralSet(varIndex));
      results.stopClock("ChangeFormula");
      DIMACSUtils.createTemporaryDimacs(temp);
      results.startClock("solver");
      binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      results.stopClock("solver");
      solverResult = solver.getResult(binaryResult.stdout);
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