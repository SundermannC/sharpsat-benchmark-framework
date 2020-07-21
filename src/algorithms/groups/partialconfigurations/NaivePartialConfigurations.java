package algorithms.groups.partialconfigurations;

import algorithms.groups.partialconfigurations.PartialConfigurationsPreprocessResult;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
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

public class NaivePartialConfigurations implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "NaivePartialConfigurations";

  public static final String ALGORITHM_GROUPID = "partialconfigurations";

  @Override
  public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file,
      List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, List<InstanceResult>> results = new HashMap<>();
    List<InstanceResult> resultPackage;
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    List<Configuration> configurations = ((PartialConfigurationsPreprocessResult)preprocessResult).configurations;
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
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      CNF temp = cnf.clone();
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(varIndex));
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(-varIndex));
      }
      DIMACSUtils.createTemporaryDimacs(temp);
      String name = "config_" + currentIndex;
      resultPackage = new ArrayList<>();
      for (IComparableSolver solver : solvers) {
        long startTime = System.nanoTime();
        binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
        long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
        runner.killCurrentProcess();
        solverResult = solver.getResult(binaryResult.stdout);
        resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
      }
      results.put(name, resultPackage);
      currentIndex++;
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
    List<Configuration> configurations = ((PartialConfigurationsPreprocessResult)preprocessResult).configurations;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
    solverResult = solver.getResult(binaryResult.stdout);
    results.put(modelName, solverResult.result.toString());
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      CNF temp = cnf.clone();
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(varIndex));
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(-varIndex));
      }
      DIMACSUtils.createTemporaryDimacs(temp);
      String name = "config_" + currentIndex;
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      solverResult = solver.getResult(binaryResult.stdout);
      results.put(name, solverResult.result.toString());
      currentIndex++;
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
    List<Configuration> configurations = ((PartialConfigurationsPreprocessResult)preprocessResult).configurations;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    results.startClock("solver");
    binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
    results.stopClock("solver");
    solverResult = solver.getResult(binaryResult.stdout);
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      results.startClock("ChangeFormula");
      CNF temp = cnf.clone();
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(varIndex));
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = temp.getVariables().getVariable(featName);
        temp.addClause(new LiteralSet(-varIndex));
      }
      results.stopClock("ChangeFormula");
      DIMACSUtils.createTemporaryDimacs(temp);
      String name = "config_" + currentIndex;
      results.startClock("solver");
      binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      results.stopClock("solver");
      solverResult = solver.getResult(binaryResult.stdout);
      currentIndex++;
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