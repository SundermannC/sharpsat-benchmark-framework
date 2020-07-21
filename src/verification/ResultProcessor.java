package verification;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import algorithms.basics.IComparableAlgorithm;
import comparablesolver.IComparableSolver;
import main.BenchmarkConfig;
import resultpackages.CompareSolverResultPackage;
import resultpackages.InstanceResult;
import resultpackages.ResultRuntimePackage;
import utils.BenchmarkConstants;
import utils.BenchmarkUtils;
import utils.BenchmarkUtils.Status;
import utils.FileUtils;

public class ResultProcessor {

	private boolean verifyTheResults;
		
	protected Map<String, String> verifyResults;
	
	/**
	 * Constructor to be called if the results should not be verificated just processed
	 */
	public ResultProcessor() {
		verifyTheResults = false;
	}
	
	public ResultProcessor(List<String> modelPaths, String algorithmId) {
		verifyTheResults = true;
		loadVerifyResults(modelPaths, algorithmId);
	}

	
	
	public void processAndSaveCompareAlgorithmsResults(Map<String, List<ResultRuntimePackage[]>> results, BenchmarkConfig config) {
		String fileName = createAlgorithmCompareResultPath(config.algorithms.get(0).getAlgorithmGroupId(), config.configFileName);
		
		// Create headers
		String runtimeCcontent = "";
		String resultContent = "";
		if (!(new File(fileName).exists())) {
			runtimeCcontent = "instance";
			resultContent = "instance";
			for (IComparableAlgorithm algorithm : config.algorithms) {
				for (IComparableSolver solver : config.solvers) {
					runtimeCcontent += ";" + solver.getIdentifier() + "_" + algorithm.getAlgorithmId() + BenchmarkUtils.getTimeUnitAttachment(config.timeUnit);					
					resultContent += ";" + solver.getIdentifier() + "_" + algorithm.getAlgorithmId();
				}
			}
		}
		

		
		for (String instance : results.keySet()) {
			runtimeCcontent += "\n" + instance;
			resultContent += "\n" + instance;
			for (ResultRuntimePackage[] packages : results.get(instance)) {
				for (ResultRuntimePackage rrPackage : packages) {
					runtimeCcontent += ";" + BenchmarkUtils.translateToTimeUnit(rrPackage.runtime, config.timeUnit);
					resultContent += ";" + createMapString(rrPackage.result);
				}
			}
		}
		FileUtils.writeContentToFileAndCreateDirs(createAlgorithmCompareRuntimePath(config.algorithms.get(0).getAlgorithmGroupId(), config.configFileName), runtimeCcontent, false);
		FileUtils.writeContentToFileAndCreateDirs(createAlgorithmCompareResultPath(config.algorithms.get(0).getAlgorithmGroupId(), config.configFileName), resultContent, false);

	}
	
	private String createMapString(Map<String, String> map) {
		String mapString = "";
		if (map == null || map.isEmpty()) {
			return mapString;
		}
		List<String> sortedKeys = new ArrayList<>(map.keySet());
		Collections.sort(sortedKeys);
		for (String key : sortedKeys) {
			mapString += key + ":" + map.get(key) + "|";
		}
		
		return mapString.substring(0, mapString.length() - 1);
	}

	
	
	/**
	 * Creates a csv with the following structure
	 * 
	 * Instance;solver1;solver2;....;solvern
	 * instanceName;<runtime|indicator(wrongresult|memorylimit);...
	 * @param results
	 */
	public void processAndSaveCompareSolverResults(CompareSolverResultPackage results, BenchmarkConfig config) {
		
		Set<String> keys = results.resultPackage.keySet();
		List<String> sortedKeys = new ArrayList<>(keys);
		Collections.sort(sortedKeys);
		boolean saveAdditonalInfo = (results.resultPackage.get(keys.iterator().next()).get(0).addititionalInfo != null);
		String fileName = createSolverCompareResultPath(results, config.configFileName);
		String result = "";
		// Create header if file does not exist
		if (!(new File(fileName).exists())) {
			result = "instance";
			for (String solverName : results.solverNames) {
				result += ";" + solverName + "RT" + BenchmarkUtils.getTimeUnitAttachment(config.timeUnit);
				result += ";" + solverName + "MemMax";
				result += ";" + solverName + "#SAT";
				if (saveAdditonalInfo) {
					result += ";" + solverName + "Info";
				}
			}
		}

		
		// Create measured runtimes/ indicators
		for (String key : sortedKeys) {
			
			result += "\n" + key;
			List<InstanceResult> packages = results.resultPackage.get(key);
			
			for (int i = 0; i < packages.size(); i++) {
				InstanceResult temp = packages.get(i);
				result += ";";
				float runtime = BenchmarkUtils.translateToTimeUnit(temp.runtime, config.timeUnit);
				if (temp.status.equals(Status.MEMORY_LIMIT_REACHED)) {
					result += BenchmarkConstants.MEMORY_LIMIT_REACHED_INDICATOR;
				} else if(temp.status.equals(Status.TIMEOUT)) {
					result += config.timeUnit.convert(config.timeLimit, TimeUnit.MINUTES); // Timeout is always specified in minutes
				} else if(temp.status.equals(Status.SOLVED)) {
					if (!verifyTheResults || verifyResult(key, temp.result.toString(), results.solverNames.get(i))) {
						result += runtime;
					} else {
						result += BenchmarkConstants.WRONG_RESULT_INDICATOR;
					}
				} else {
					result += BenchmarkConstants.UNEXPECTED_ERROR_INDICATOR;
				}
				result += ";" + temp.maxMemory + ";" + temp.result.toString();
				if (saveAdditonalInfo) {
					result += ";" + temp.addititionalInfo;
				}
			}
		}
		FileUtils.writeContentToFileAndCreateDirs(createSolverCompareResultPath(results, config.configFileName), result, false);
	}
	
	private String createSolverCompareResultPath(CompareSolverResultPackage results, String configName) {
		return BenchmarkConstants.COMPARE_SOLVER_RESULTS_DIRECTORY + results.algorithmId + File.separator + configName + ".csv";
	}
	
	private String createAlgorithmCompareRuntimePath(String algorithmGroupId, String configName) {
		return BenchmarkConstants.COMPARE_ALGORITHM_RESULTS_DIRECTORY + algorithmGroupId + File.separator + configName + ".csv";
	}
	
	private String createAlgorithmCompareResultPath(String algorithmGroupId, String configName) {
		return BenchmarkConstants.COMPARE_ALGORITHM_RESULTS_DIRECTORY + algorithmGroupId + File.separator + configName + "_" + "results.csv";
	}
	
	
	public boolean verifyResults(Map<String, String> results, String solver) {
		for (String key : results.keySet()) {
			if (!verifyResult(key, results.get(key), solver)) {
				return false;
			}
		}
		return true;
	}

	public boolean verifyResult(String resultIdentifier, String result, String solver) {
		if (!verifyResults.containsKey(resultIdentifier)) {
			System.out.println("Unexpected resultidentifier by " + solver + ": " + resultIdentifier);
			return false;
		}
		if (verifyResults.get(resultIdentifier).startsWith("-")) {
			System.out.println("Result cannot be verified for: " + resultIdentifier + "(Result unknown)");
			return true;
		}
		if (verifyResults.get(resultIdentifier).equals(result)) {
			return true;
		} else {
			System.out.println("Wrong result by " + solver + " for identifier " + resultIdentifier);
			return false;
		}
		
	}

	public void loadVerifyResults(List<String> modelPaths, String algorithmId) {
		for (String path : modelPaths) {
			List<String[]> verifyRows;
			String verifyPath = createVerifyPath(path, algorithmId);
			try {
				verifyRows = FileUtils.readCsvFile(verifyPath, ";");
				verifyResults = new HashMap<>();
				
				for (String[] row : verifyRows) {
					verifyResults.put(row[0], row[1]);
				}
			} catch (FileNotFoundException e) {
				System.out.println("Verification file not found:" + verifyPath);
				System.exit(-1);
			}

		}
	
	}
	
	private String createVerifyPath(String modelPath, String algorithmId) {
		String modelName = FileUtils.getFileNameWithoutExtension(modelPath);
		return BenchmarkConstants.VERIFY_DIRECTORY + algorithmId + File.separator + modelName;
	}
	

	
	
}
