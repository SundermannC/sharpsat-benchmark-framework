package algorithms.groups.commonality;

import java.util.HashSet;
import java.util.Set;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
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

public class PropogateTreePropertiesCommonality implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "PropogateTreePropertiesCommonality";

  public static final String ALGORITHM_GROUPID = "commonality";

  @Override
  public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file,
      List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, List<InstanceResult>> results = new HashMap<>();
    List<InstanceResult> resultPackage;
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String overallModelCount = "";
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    Set<String> treeCoreFeatures = FMUtils.getCoreFeatureNamesByTree(model);
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
    overallModelCount = solverResult.result.toString();
    FeatureModelFormula formula = new FeatureModelFormula(model);
    CNF cnf = formula.getCNF();
    for (IFeature feat : model.getFeatures())  {
      String featName = feat.getName();
      String currentName = modelName + "_" + featName;
      if (treeCoreFeatures.contains(featName))  {
        } else  {
          CNF temp = cnf.clone();
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
          results.put(currentName, resultPackage);
        }
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
      String overallModelCount = "";
      String modelName = FileUtils.getFileNameWithoutExtension(file);
      IFeatureModel model = FMUtils.readFeatureModel(file);
      Set<String> treeCoreFeatures = FMUtils.getCoreFeatureNamesByTree(model);
      FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      solverResult = solver.getResult(binaryResult.stdout);
      results.put(modelName, solverResult.result.toString());
      overallModelCount = solverResult.result.toString();
      FeatureModelFormula formula = new FeatureModelFormula(model);
      CNF cnf = formula.getCNF();
      for (IFeature feat : model.getFeatures())  {
        String featName = feat.getName();
        String currentName = modelName + "_" + featName;
        if (treeCoreFeatures.contains(featName))  {
          results.put(featName, overallModelCount);
          } else  {
            CNF temp = cnf.clone();
            int varIndex = temp.getVariables().getVariable(featName);
            temp.addClause(new LiteralSet(varIndex));
            DIMACSUtils.createTemporaryDimacs(temp);
            binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
            solverResult = solver.getResult(binaryResult.stdout);
            results.put(currentName, solverResult.result.toString());
          }
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
        String overallModelCount = "";
        String modelName = FileUtils.getFileNameWithoutExtension(file);
        IFeatureModel model = FMUtils.readFeatureModel(file);
        Set<String> treeCoreFeatures = FMUtils.getCoreFeatureNamesByTree(model);
        FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
        results.startClock("solver");
        binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
        results.stopClock("solver");
        solverResult = solver.getResult(binaryResult.stdout);
        overallModelCount = solverResult.result.toString();
        FeatureModelFormula formula = new FeatureModelFormula(model);
        CNF cnf = formula.getCNF();
        for (IFeature feat : model.getFeatures())  {
          String featName = feat.getName();
          String currentName = modelName + "_" + featName;
          if (treeCoreFeatures.contains(featName))  {
            } else  {
              results.startClock("ChangeFormula");
              CNF temp = cnf.clone();
              int varIndex = temp.getVariables().getVariable(featName);
              temp.addClause(new LiteralSet(varIndex));
              results.stopClock("ChangeFormula");
              DIMACSUtils.createTemporaryDimacs(temp);
              results.startClock("solver");
              binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
              results.stopClock("solver");
              solverResult = solver.getResult(binaryResult.stdout);
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