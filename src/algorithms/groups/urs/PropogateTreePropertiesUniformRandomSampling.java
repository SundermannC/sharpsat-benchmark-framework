package algorithms.groups.urs;

import java.math.BigInteger;
import java.util.Random;
import java.util.Set;
import algorithms.groups.urs.URSPreprocessorResult;
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

public class PropogateTreePropertiesUniformRandomSampling implements IComparableAlgorithm {
  public static final String ALGORITHM_ID = "PropogateTreePropertiesUniformRandomSampling";

  public static final String ALGORITHM_GROUPID = "urs";

  @Override
  public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file,
      List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws
      InterruptedException {
    Map<String, List<InstanceResult>> results = new HashMap<>();
    List<InstanceResult> resultPackage;
    URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
    BinaryResult binaryResult = null;
    SolverResult solverResult = null;
    String modelName = FileUtils.getFileNameWithoutExtension(file);
    IFeatureModel model = FMUtils.readFeatureModel(file);
    Set<IFeature> coreFeatures = FMUtils.getCoreFeatures(model);
    Set<IFeature> deadFeatures = FMUtils.getDeadFeatures(model);
    Set<IFeature> falseOptionalFeatures = FMUtils.getFalseOptionalFeatures(model);
    FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
    resultPackage = new ArrayList<>();
    for (IComparableSolver solver : solvers) {
      long startTime = System.nanoTime();
      binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
      long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
      runner.killProcessesByUserAndName(solver.getBinaryName());
      solverResult = solver.getResult(binaryResult.stdout);
      resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
    }
    results.put(modelName, resultPackage);
    for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
      BigInteger originalRandomNumber = randomNumber;
      FeatureModelFormula formula = new FeatureModelFormula(model);
      CNF cnf = formula.getCNF();
      List<String> includedFeatures = new ArrayList<>();
      for (IFeature feat : FMUtils.getFeaturesInOrder(model))  {
        String featName = feat.getName();
        int varIndex = cnf.getVariables().getVariable(featName);
        if (coreFeatures.contains(feat))  {
          includedFeatures.add(featName);
          cnf.addClause(new LiteralSet(varIndex));
          } else if (deadFeatures.contains(feat))  {
            cnf.addClause(new LiteralSet(-varIndex));
            } else if (!includedFeatures.contains(feat.getStructure().getParent().getFeature().getName()))  {
              cnf.addClause(new LiteralSet(-varIndex));
              } else if (feat.getStructure().isMandatory() || falseOptionalFeatures.contains(feat))  {
                includedFeatures.add(featName);
                cnf.addClause(new LiteralSet(varIndex));
                } else  {
                  CNF temp = cnf.clone();
                  temp.addClause(new LiteralSet(-varIndex));
                  DIMACSUtils.createTemporaryDimacs(temp);
                  resultPackage = new ArrayList<>();
                  for (IComparableSolver solver : solvers) {
                    long startTime = System.nanoTime();
                    binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
                    long runtime = BenchmarkUtils.getDurationNano(startTime, System.nanoTime());
                    runner.killProcessesByUserAndName(solver.getBinaryName());
                    solverResult = solver.getResult(binaryResult.stdout);
                    resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout));
                  }
                  results.put(featName, resultPackage);
                  if (solverResult.result.compareTo(randomNumber) >= 0)  {
                    cnf.addClause(new LiteralSet(-varIndex));
                    } else  {
                      includedFeatures.add(featName);
                      cnf.addClause(new LiteralSet(varIndex));
                      randomNumber = randomNumber.subtract(solverResult.result);
                    }
                  }
                }
                String resultString = String.join(",", includedFeatures);
              }
              return new CompareSolverResultPackage(solvers, results, ALGORITHM_ID);
            }

            @Override
            public Map<String, String> measureRuntime(BinaryRunner runner, String file,
                IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws
                InterruptedException {
              Map<String, String> results = new HashMap<>();
              URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
              BinaryResult binaryResult = null;
              SolverResult solverResult = null;
              String modelName = FileUtils.getFileNameWithoutExtension(file);
              IFeatureModel model = FMUtils.readFeatureModel(file);
              Set<IFeature> coreFeatures = FMUtils.getCoreFeatures(model);
              Set<IFeature> deadFeatures = FMUtils.getDeadFeatures(model);
              Set<IFeature> falseOptionalFeatures = FMUtils.getFalseOptionalFeatures(model);
              FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
              binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
              solverResult = solver.getResult(binaryResult.stdout);
              for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
                BigInteger originalRandomNumber = randomNumber;
                FeatureModelFormula formula = new FeatureModelFormula(model);
                CNF cnf = formula.getCNF();
                List<String> includedFeatures = new ArrayList<>();
                for (IFeature feat : FMUtils.getFeaturesInOrder(model))  {
                  String featName = feat.getName();
                  int varIndex = cnf.getVariables().getVariable(featName);
                  if (coreFeatures.contains(feat))  {
                    includedFeatures.add(featName);
                    cnf.addClause(new LiteralSet(varIndex));
                    } else if (deadFeatures.contains(feat))  {
                      cnf.addClause(new LiteralSet(-varIndex));
                    } else if (!includedFeatures.contains(feat.getStructure().getParent().getFeature().getName()))  {
                        cnf.addClause(new LiteralSet(-varIndex));
                    } else if (feat.getStructure().isMandatorySet() || falseOptionalFeatures.contains(feat))  {
                          includedFeatures.add(featName);
                          cnf.addClause(new LiteralSet(varIndex));
                    } else  {
                        CNF temp = cnf.clone();
                        temp.addClause(new LiteralSet(-varIndex));
                        DIMACSUtils.createTemporaryDimacs(temp);
                        binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
                        solverResult = solver.getResult(binaryResult.stdout);
                            if (solverResult.result.compareTo(randomNumber) >= 0)  {
                              cnf.addClause(new LiteralSet(-varIndex));
                              } else  {
                                includedFeatures.add(featName);
                                cnf.addClause(new LiteralSet(varIndex));
                                randomNumber = randomNumber.subtract(solverResult.result);
                              }
                            }
                          }
                          String resultString = String.join(",", includedFeatures);
                          results.put(modelName + originalRandomNumber.toString(), resultString);
                        }
                        return results;
                      }

                      @Override
                      public Map<String, String> measureRuntime(BinaryRunner runner,
                          List<String> files, IComparableSolver solver, int timeout,
                          IPreprocessResult preprocessResult) throws InterruptedException {
                        Map<String, String> results = new HashMap<>();
                        for(String file : files) {
                          Map<String, String> interimResult = measureRuntime(runner,file, solver, timeout, preprocessResult);
                          results.putAll(interimResult);
                        }
                        return results;
                      }

                      @Override
                      @SuppressWarnings("unused")
                      public PreciseAnalysisResultPackage preciseAnalysis(BinaryRunner runner,
                          String file, IComparableSolver solver, int timeout,
                          IPreprocessResult preprocessResult) throws InterruptedException {
                        PreciseAnalysisResultPackage results = new PreciseAnalysisResultPackage(file);
                        Runtime runtime = Runtime.getRuntime();
                        System.gc();
                        long startMemory = runtime.totalMemory() - runtime.freeMemory();
                        long maxMemory = 0;
                        long tempMemory;
                        String maxMemorySource = "";
                        URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
                        BinaryResult binaryResult = null;
                        SolverResult solverResult = null;
                        String modelName = FileUtils.getFileNameWithoutExtension(file);
                        IFeatureModel model = FMUtils.readFeatureModel(file);
                        Set<IFeature> coreFeatures = FMUtils.getCoreFeatures(model);
                        Set<IFeature> deadFeatures = FMUtils.getDeadFeatures(model);
                        Set<IFeature> falseOptionalFeatures = FMUtils.getFalseOptionalFeatures(model);
                        FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
                        results.startClock("solver");
                        binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
                        results.stopClock("solver");
                        solverResult = solver.getResult(binaryResult.stdout);
                        for (BigInteger randomNumber : ursPreprocessResult.randomNumbers)  {
                          BigInteger originalRandomNumber = randomNumber;
                          FeatureModelFormula formula = new FeatureModelFormula(model);
                          CNF cnf = formula.getCNF();
                          List<String> includedFeatures = new ArrayList<>();
                          for (IFeature feat : FMUtils.getFeaturesInOrder(model))  {
                            String featName = feat.getName();
                            int varIndex = cnf.getVariables().getVariable(featName);
                            if (coreFeatures.contains(feat))  {
                              includedFeatures.add(featName);
                              cnf.addClause(new LiteralSet(varIndex));
                              } else if (deadFeatures.contains(feat))  {
                                cnf.addClause(new LiteralSet(-varIndex));
                                } else if (!includedFeatures.contains(feat.getStructure().getParent().getFeature().getName()))  {
                                  cnf.addClause(new LiteralSet(-varIndex));
                                  } else if (feat.getStructure().isMandatory() || falseOptionalFeatures.contains(feat))  {
                                    includedFeatures.add(featName);
                                    cnf.addClause(new LiteralSet(varIndex));
                                    } else  {
                                      results.startClock("ChangeFormula");
                                      CNF temp = cnf.clone();
                                      temp.addClause(new LiteralSet(-varIndex));
                                      results.stopClock("ChangeFormula");
                                      DIMACSUtils.createTemporaryDimacs(temp);
                                      results.startClock("solver");
                                      binaryResult = solver.executeSolver(runner, DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
                                      results.stopClock("solver");
                                      solverResult = solver.getResult(binaryResult.stdout);
                                      results.startClock("ProcessCount");
                                      if (solverResult.result.compareTo(randomNumber) >= 0)  {
                                        cnf.addClause(new LiteralSet(-varIndex));
                                        } else  {
                                          includedFeatures.add(featName);
                                          cnf.addClause(new LiteralSet(varIndex));
                                          randomNumber = randomNumber.subtract(solverResult.result);
                                        }
                                        results.stopClock("ProcessCount");
                                      }
                                    }
                                    String resultString = String.join(",", includedFeatures);
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