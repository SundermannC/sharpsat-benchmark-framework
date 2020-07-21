package algorithms.groups.random;

import java.util.Random;
import algorithms.groups.SeededRandom;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.FMUtils;
import utils.FileUtils;

import algorithms.basics.IComparableAlgorithm;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import ddnnfparsing.DdnnfAnalysis;

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
import utils.BenchmarkConstants;
import utils.BenchmarkUtils;
import utils.BinaryRunner;
import utils.DIMACSUtils;

public class RandomOrderDdnnf implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "RandomOrderDdnnf";

  public static final String ALGORITHM_GROUPID = "random";

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
    Random random = ((SeededRandom)preprocessResult).random;
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    int cnfClauses = cnf.getClauses().size();
    int numberOfLiterals = 0;
    for (LiteralSet clause : cnf.getClauses())  {
      numberOfLiterals += clause.getLiterals().length;
    }
    for (int i = 1; i <= 20; i++)  {
      DIMACSUtils.createTemporaryRandomizedDimacs(cnf, random);
      resultPackage = new ArrayList<>();
      for (IComparableSolver solver : solvers) {
        long startTime = System.nanoTime();
        binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
        long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
        runner.killProcessesByUserAndName(solver.getBinaryName());
        solverResult = solver.getResult(binaryResult.stdout);
        // Manual edit
        InstanceResult instanceResult = InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout);
        DdnnfAnalysis analysis = new DdnnfAnalysis(BenchmarkConstants.DDNNF_TEMP_PATH);
        String additionalInfo = cnfClauses + "," + numberOfLiterals + "," + analysis.numberOfNodes + "," + analysis.numberOfEdges + "," + analysis.fileSize;
        instanceResult.addAdditionalInfo(additionalInfo);
        resultPackage.add(instanceResult);

      }
      results.put(modelName + "_" + i, resultPackage);
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
    Random random = ((SeededRandom)preprocessResult).random;
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    int cnfClauses = cnf.getClauses().size();
    int numberOfLiterals = 0;
    for (LiteralSet clause : cnf.getClauses())  {
      numberOfLiterals += clause.getLiterals().length;
    }
    for (int i = 1; i <= 20; i++)  {
      DIMACSUtils.createTemporaryRandomizedDimacs(cnf, random);
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
      solverResult = solver.getResult(binaryResult.stdout);
      results.put(modelName + i, solverResult.result.toString());
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
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    System.gc();
    tempMemory = startMemory - (runtime.totalMemory() - runtime.freeMemory());
    if (maxMemory < tempMemory) {
      maxMemory = tempMemory;
      maxMemorySource = "ReadModel";
    }
    Random random = ((SeededRandom)preprocessResult).random;
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    int cnfClauses = cnf.getClauses().size();
    int numberOfLiterals = 0;
    for (LiteralSet clause : cnf.getClauses())  {
      numberOfLiterals += clause.getLiterals().length;
    }
    for (int i = 1; i <= 20; i++)  {
      DIMACSUtils.createTemporaryRandomizedDimacs(cnf, random);
      results.startClock("solver");
      binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
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