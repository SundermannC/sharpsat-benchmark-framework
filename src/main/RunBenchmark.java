package main;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import algorithms.basics.IComparableAlgorithm;
import algorithms.groups.IPreprocessResult;
import algorithms.groups.TimeoutPreprocess;
import main.BenchmarkConfig.BenchmarkType;
import resultpackages.CompareSolverResultPackage;
import resultpackages.PreciseAnalysisResultPackage;
import resultpackages.ResultRuntimePackage;
import utils.BenchmarkConstants;
import utils.BenchmarkUtils;
import utils.BinaryRunner;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;
import verification.ResultProcessor;

public class RunBenchmark {

	BenchmarkConfig config;
	BinaryRunner runner;
	
	public RunBenchmark(BenchmarkConfig config) {
		this.config = config;
		this.runner = new BinaryRunner(config.timeLimit, config.username);
	}


	public static void main(String[] args) {
		FMUtils.installLibraries();
		String input = "";
		
		if ((args.length == 1)) {
			input = args[0];
		} else {
			System.out.println("Specify config? Relative path from configs/");
			Scanner in = new Scanner(System.in); 
	        input = in.nextLine();
	        in.close();
		}
		BenchmarkConfig config = new BenchmarkConfig(input);
		RunBenchmark instance = new RunBenchmark(config);
		instance.run();
		instance.cleanup();
	}
	
	
	private void cleanup() {
		FileUtils.deleteFile(BenchmarkConstants.DDNNF_TEMP_PATH);
		FileUtils.deleteFile(DIMACSUtils.TEMPORARY_DIMACS_PATH);
	}
	
	private void run() {
		if (config.type.equals(BenchmarkType.COMPARE_SOLVERS)) {
			runCompareSolvers();
		} else if (config.type.equals(BenchmarkType.COMPARE_ALGORITHMS)) {
			runCompareAlgorithms();
		} else if (config.type.equals(BenchmarkType.PRECISE_ANALYSIS)) {
			runPreciseAnalysis();
		}
	}
	

	/**
	 * 
	 * @param solvers
	 * @param algorithms
	 * @param timelimit
	 */
	private void runCompareSolvers() {
		for (IComparableAlgorithm algorithm : config.algorithms) {
			compareSolvers(algorithm);
		}
	}
	
	
	private void runCompareAlgorithms() {
		Map<String, List<ResultRuntimePackage[]>> runtimes = new HashMap<>();
		ResultProcessor processor;
		if (config.verifyResults) {
			processor = new ResultProcessor(config.files, config.algorithms.get(0).getAlgorithmGroupId());
		} else {
			processor = new ResultProcessor();
		}
		
		// Warm-up
		System.out.println("Performing warm-up!");
		
		for (int i = 0; i < 10; i++) {
			List<ResultRuntimePackage[]> algorithmResults = new ArrayList<>();
			IPreprocessResult ppResult = null;
			try {
				ppResult = config.algGroupPreprocessor.getPreprocessData(config.random, config.solvers.get(0), config.warmupModel, config.timeLimit);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (IComparableAlgorithm algorithm : config.algorithms) {
				algorithmResults.add(runSingleAlgorithm(algorithm, config.warmupModel, processor, ppResult));
			}
			runtimes.put(FileUtils.getFileNameWithoutExtension(config.warmupModel), algorithmResults);
		}
		
		runtimes = new HashMap<>();
		
		System.out.println("Starting actual experiments");
		
		List<List<String>> increments = createIncrements();

		
		// Actual computation
		for (int i = config.processedIncrements; i < increments.size(); i++) {
			if (config.separateFiles) {
				for (String file : increments.get(i)) {
					List<ResultRuntimePackage[]> algorithmResults = new ArrayList<>();
					IPreprocessResult ppResult = null;
					try {
						ppResult = config.algGroupPreprocessor.getPreprocessData(config.random, config.solvers.get(0), file, config.timeLimit);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					for (IComparableAlgorithm algorithm : config.algorithms) {
						algorithmResults.add(runSingleAlgorithm(algorithm, file, processor, ppResult));
					}
					runtimes.put(FileUtils.getFileNameWithoutExtension(file), algorithmResults);
				}
			} 
			processor.processAndSaveCompareAlgorithmsResults(runtimes, config);
			runtimes = new HashMap<>();
			updateBackupInfo();
		}
	}
	
	private void runPreciseAnalysis() {
		List<List<String>> increments = createIncrements();
		for (int i = config.processedIncrements + 1; i < increments.size(); i++) {

			// TODO: handle timeout on preprocessing
			List<PreciseAnalysisResultPackage> results = new ArrayList<>();
			for (String file : increments.get(i)) {
				IPreprocessResult ppResult = null;
				try {
					ppResult = config.algGroupPreprocessor.getPreprocessData(config.random, config.solvers.get(0), file, config.timeLimit);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				try {
					results.add(config.algorithms.get(0).preciseAnalysis(runner,file, config.solvers.get(0), config.timeLimit, ppResult));
				} catch (InterruptedException e) {
				}
			}
			PreciseAnalysisResultPackage.writeResultsToFile(results, config);
			updateBackupInfo();
		}
	}
	
	private ResultRuntimePackage[] runSingleAlgorithm(IComparableAlgorithm algorithm, String file, ResultProcessor processor, IPreprocessResult ppResult) {
		ResultRuntimePackage[] results = new ResultRuntimePackage[config.solvers.size()];

		final IPreprocessResult preprocessResult = ppResult;
				
		for (int i = 0; i < results.length; i++) {
			ResultRuntimePackage result = null;
			if (ppResult instanceof TimeoutPreprocess) {
				results[i] = new ResultRuntimePackage(null, TimeUnit.NANOSECONDS.convert(config.timeLimit, TimeUnit.MINUTES));
				continue;
			}
			final int index = i;
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<ResultRuntimePackage> future = executor.submit(new Callable<ResultRuntimePackage>() {

			    public ResultRuntimePackage call() throws Exception {
			    	long startSolverExe = System.nanoTime();
			        Map<String, String> result = algorithm.measureRuntime(runner,file, config.solvers.get(index), config.timeLimit, preprocessResult);
			        long endSolverExe = System.nanoTime();
			        long durationSolverExe = BenchmarkUtils.getDurationNano(startSolverExe, endSolverExe);
			        return new ResultRuntimePackage(result, durationSolverExe);
			    }
			});
			try {
			    result = future.get(config.timeLimit, TimeUnit.MINUTES);
			} catch (TimeoutException e) {
				// Timeout
				runner.killCurrentProcess();
			    result = new ResultRuntimePackage(null, TimeUnit.NANOSECONDS.convert(config.timeLimit, TimeUnit.MINUTES));
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			BinaryRunner.killProcessesByUserAndName(config.username, config.solvers.get(i).getBinaryName());
			executor.shutdownNow();
			try {
				executor.awaitTermination(100, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			}
			// Result is not correct
			if (config.verifyResults && !processor.verifyResults(result.result, config.solvers.get(0).getIdentifier())) {
					results[i] = result;
			} else {
				results[i] = result;
			}
		}

		return results;
	}
	
//	private Long[] runSingleAlgorithm(IComparableAlgorithm algorithm, List<String> files, ResultProcessor processor) {
//		Long[] results = new Long[config.solvers.size()];
//		for (int i = 0; i < results.length; i++) {
//			long startSolverExe = System.nanoTime();
//			Map<String, String> result = null;
//			try {
//				result = algorithm.measureRuntime(runner,files, config.solvers.get(i), config.timeLimit);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			long endSolverExe = System.nanoTime();
//			long durationSolverExe = BenchmarkUtils.getDurationNano(startSolverExe, endSolverExe);
//			// Result is not correct
//			if (config.verifyResults && !processor.verifyResults(result, config.solvers.get(i).getIdentifier())) {
//					results[i] = -durationSolverExe;
//			} else {
//				results[i] = durationSolverExe;
//			}
//		}
//		return results;
//	}
	
	
	private void compareSolvers(IComparableAlgorithm algorithm) {
		ResultProcessor processor;
		if (config.verifyResults) {
			processor = new ResultProcessor(config.files, algorithm.getAlgorithmGroupId());
		} else {
			processor = new ResultProcessor();
		}
		
		// Warm-up
		System.out.println("Performing warm-up!");
		
		for (int i = 0; i < 10; i++) {
			CompareSolverResultPackage results = new CompareSolverResultPackage();
				IPreprocessResult ppResult = null;
				try {
					ppResult = config.algGroupPreprocessor.getPreprocessData(config.random, config.solvers.get(0), config.warmupModel, config.timeLimit);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					return;
				}
				try {
					results.addNewResults(algorithm.compareSolvers(runner,config.warmupModel, config.solvers, config.timeLimit, ppResult));
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
		}
		
		
		System.out.println("Starting actual experiments");
		
		List<List<String>> increments = createIncrements();
		
		for (int i = config.processedIncrements; i < increments.size(); i++) {
			CompareSolverResultPackage results = new CompareSolverResultPackage();
			for (String file : increments.get(i)) {
				IPreprocessResult ppResult = null;
				try {
					ppResult = config.algGroupPreprocessor.getPreprocessData(config.random, config.solvers.get(0), file, config.timeLimit);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				boolean preprocessTimeout = ppResult instanceof TimeoutPreprocess; // TODO: handle this later
				try {
					results.addNewResults(algorithm.compareSolvers(runner,file, config.solvers, config.timeLimit, ppResult));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			processor.processAndSaveCompareSolverResults(results, config);
			updateBackupInfo();
		}

	}
	
	
	private void updateBackupInfo() {
		config.updateProcessedCount();
		int processedModels = config.incrementSize * config.processedIncrements;
		int numberOfModels = config.files.size();
		if (processedModels > numberOfModels) {
			processedModels = numberOfModels;
		}
		System.out.println("Evaluated " + processedModels +  "/" + numberOfModels 
				+ " models. Backup at:" + new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(new Date()).toString());
		
	}
	
	
	private List<List<String>> createIncrements() {
		List<List<String>> increments = new ArrayList<>();
		List<String> currentIncrement = new ArrayList<>();
		
		for (int i = 0; i < config.files.size();  i++) {
			currentIncrement.add(config.files.get(i));
			if ((i + 1) % config.incrementSize == 0) {
				increments.add(currentIncrement);
				currentIncrement = new ArrayList<>();
			}
		}
		if (!currentIncrement.isEmpty()) {
			increments.add(currentIncrement);
		}
		
		return increments;
	}

	
}
