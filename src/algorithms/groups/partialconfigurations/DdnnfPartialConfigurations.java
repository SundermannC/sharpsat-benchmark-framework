package algorithms.groups.partialconfigurations;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import algorithms.groups.partialconfigurations.PartialConfigurationsPreprocessResult;
import ddnnfparsing.DDNNFPropFormat;
import ddnnfparsing.iterativebottomup.IterativeBottomUpDdnnfFormat;
import ddnnfparsing.optimized.OptimizedDdnnfFormat;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
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

public class DdnnfPartialConfigurations implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "DdnnfPartialConfigurations";

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
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    resultPackage = new ArrayList<>();
    for (IComparableSolver solver : solvers) {
      long startTime = System.nanoTime();
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
      long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
      runner.killCurrentProcess();
      solverResult = solver.getResult(binaryResult.stdout);
      resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
    }
    results.put(modelName, resultPackage);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      Set<Integer> included = new HashSet<>();
      Set<Integer> excluded = new HashSet<>();
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        included.add(varIndex);
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        excluded.add(varIndex);
      }
      String name = "config_" + currentIndex;
      BigInteger result = format.getPartialConfigurationCount(included, excluded);
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
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    solverResult = solver.getResult(binaryResult.stdout);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      Set<Integer> included = new HashSet<>();
      Set<Integer> excluded = new HashSet<>();
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        included.add(varIndex);
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        excluded.add(varIndex);
      }
      String name = "config_" + currentIndex;
      BigInteger result = format.getPartialConfigurationCount(included, excluded);
      results.put(name, result.toString());
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
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    DIMACSUtils.createTemporaryDimacs(cnf);
    results.startClock("solver");
    binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout,true);
    results.stopClock("solver");
    solverResult = solver.getResult(binaryResult.stdout);
    IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
    format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
    System.gc();
    tempMemory = startMemory - (runtime.totalMemory() - runtime.freeMemory());
    if (maxMemory < tempMemory) {
      maxMemory = tempMemory;
      maxMemorySource = "ddnnf";
    }
    int currentIndex = 1;
    for (Configuration config : configurations)  {
      Set<Integer> included = new HashSet<>();
      Set<Integer> excluded = new HashSet<>();
      results.startClock("ParseConfig");
      for (IFeature feat : config.getSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        included.add(varIndex);
      }
      for (IFeature feat : config.getUnSelectedFeatures())  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        excluded.add(varIndex);
      }
      results.stopClock("ParseConfig");
      String name = "config_" + currentIndex;
      BigInteger result = format.getPartialConfigurationCount(included, excluded);
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