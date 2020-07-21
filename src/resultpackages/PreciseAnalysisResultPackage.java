package resultpackages;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithms.basics.IComparableAlgorithm;
import comparablesolver.IComparableSolver;
import main.BenchmarkConfig;
import utils.BenchmarkConstants;
import utils.BenchmarkUtils;
import utils.FileUtils;

public class PreciseAnalysisResultPackage {
	
	private Map<String, Long> startTimes;
	
	public String file;
	
	private Map<String, Long> currentFileTimes;
	
	public int currentNumberOfSolverCalls;
	
	public long maxMemory;
	
	public String maxMemorySource;
	
	
	public PreciseAnalysisResultPackage(String file) {
		this.file = file;
		this.startTimes = new HashMap<String, Long>();
		this.currentFileTimes = new HashMap<>();
		this.currentNumberOfSolverCalls = 0;
		this.maxMemory = 0;
		this.maxMemorySource = "";
	}
	
	public void startClock(String param) {
		if (param.equals("solver")) {
			currentNumberOfSolverCalls++;
		}
		startTimes.put(param, System.nanoTime());
	}
	
	public void stopClock(String param) {
		Long endtime = System.nanoTime();
		Long runtime = BenchmarkUtils.getDurationNano(startTimes.get(param), endtime);
		if(currentFileTimes.containsKey(param)) {
			currentFileTimes.put(param, currentFileTimes.get(param) + runtime);
		} else {
			currentFileTimes.put(param, runtime);
		}
	}
	
	private String getResultString(BenchmarkConfig config) {
		String resultString = file;
		for (String key : currentFileTimes.keySet()) {
			resultString += ";" + BenchmarkUtils.translateToTimeUnit(currentFileTimes.get(key), config.timeUnit);
		}
		resultString += ";" + currentNumberOfSolverCalls;
		resultString += ";" + maxMemory;
		resultString += ";" + maxMemorySource;
		return resultString;
	}
	
	public static void writeResultsToFile(List<PreciseAnalysisResultPackage> results, BenchmarkConfig config) {
		String fileName = createPreciseAnalysisResultPath(config.configFileName, config.algorithms.get(0).getAlgorithmId(), config.algorithms.get(0).getAlgorithmGroupId());
		String content = "";
		Set<String> keyset = results.get(0).currentFileTimes.keySet();
		
		if (!(new File(fileName).exists())) {
			content += "file";
			for (String key : keyset) {
				content += ";" + key + BenchmarkUtils.getTimeUnitAttachment(config.timeUnit);
			}
			content += ";" + "#solverCalls";
			content += ";" + "memoryPeak";
			content += ";" + "memoryPeakSource";
		}

		for(PreciseAnalysisResultPackage result : results) {
			content += "\n" + result.getResultString(config);
		}
		
		FileUtils.writeContentToFileAndCreateDirs(fileName, content, false);
	}

	
	private static String createPreciseAnalysisResultPath(String configFileName,String algorithm, String algorithmGroup) {
		String timestamp = new SimpleDateFormat("yyyyMMdd,HH:mm:ss").format(new Date());
		return BenchmarkConstants.PRECISE_ANALYSIS_RESULTS_DIRECTORY + algorithmGroup
				+ File.separator + algorithm + File.separator +  configFileName + "_" + timestamp + ".csv";
	}
	
	
}
