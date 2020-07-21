package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import algorithms.basics.AlgorithmProvider;
import algorithms.basics.IComparableAlgorithm;
import algorithms.groups.GroupPreprocessorProvider;
import algorithms.groups.IGroupPreprocessor;
import comparablesolver.IComparableSolver;
import comparablesolver.SolverProvider;
import utils.BenchmarkConstants;
import utils.FileUtils;



/**
 * 
 * @author Chico Sundermann
 *
 */
public class BenchmarkConfig {
	
	public final static String LIST_SEPARATOR = ",";
	
	
	private final static String[] COMPARE_SOLVERS_TYPES =  {"comparesolvers", "cs", "compareSolvers"};
	
	private final static String[] COMPARE_ALGORITHMS_TYPES =  {"comparealgorithms", "ca", "compareAlgorithms"};
	
	private final static String[] PRECISE_ANALYSIS_TYPES = { "preciseAnalysis", "pa", "preciseanalysis"};
	
	private final static String[] NANO_STRINGS = {"ns","nanoSeconds", "nanoseconds", "nano"};
	
	private final static String[] MILLI_STRINGS = {"ms", "milliSeconds", "milliseconds", "milli"};
		
	private final static String[] SECONDS_STRINGS = {"s", "seconds"};
	
	private final static String[] MINUTES_STRINGS = {"m", "minutes"};
	
	private final static String[] HOURS_STRINGS = {"h", "hours"};
	
	public final static String TIMEUNIT_ARG = "timeUnit";
	
	public final static String BENCHMARKTYPE_ARG = "type";
	
	public final static String MEMORYLIMIT_ARG = "memorylimit";
	
	public final static String TIMELIMIT_ARG = "timelimit";
	
	public final static String SEPARATE_FILES = "separateFiles";
	
	public final static String INCREMENT_SIZE = "incrementSize";
	
	public final static String RANDOMSEED_ARG = "randomSeed";
	
	public final static String SOLVERS_ARG = "solvers";
	
	public final static String ALGORITHMS_ARG = "algorithms";
	
	public final static String VERIFYRESULTS_ARG = "verifyResults";
	
	public final static String PROCESSED_INCREMENTS_ARG = "processedIncrements";
	
	public final static String WARMUP_MODEL_ARG = "warmupmodel";
	
	
	public final static String FILES_ARG = "files";
	
	public final static String SELECTION_ALL = "all";
		

	
	
	
	
	public final static int NO_INCREMENTS = 0;
	
	public enum BenchmarkType {
		COMPARE_SOLVERS, COMPARE_ALGORITHMS, PRECISE_ANALYSIS
	}
	
	
	public BenchmarkType type;
	public List<IComparableSolver> solvers;
	public List<IComparableAlgorithm> algorithms;
	public boolean verifyResults = false;
	public int memoryLimit = 8000;
	public int timeLimit = 600;
	public List<String> files;
	public boolean separateFiles = true;
	public String configFileName;
	public int processedIncrements = 0;
	public TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	public String username;
	public IGroupPreprocessor algGroupPreprocessor;
	public Random random = new Random();
	public String warmupModel = "warmup/model.xml";
	
	public String path;
	
	public int incrementSize = 50;
	
	public BenchmarkConfig(String path) {
		this.path = createPath(path);
		configFileName = FileUtils.getFileNameWithoutExtension(path);
		username = System.getProperty("user.name");
		parseFile();
		algGroupPreprocessor = GroupPreprocessorProvider.getGroupProcessorByGroupIds(algorithms);

		performValidityCheck();
	}
	
	
	private void performValidityCheck() {
		if (solvers == null) {
			System.out.println("Provided no value for solvers. Use " + SOLVERS_ARG);
			System.exit(-1);
		}
		if (algorithms == null) {
			System.out.println("Provided no value for algorithms. Use " + ALGORITHMS_ARG);
			System.exit(-1);
		}
		if (files == null) {
			System.out.println("Provided no value for solvers. Use " + SOLVERS_ARG);
			System.exit(-1);
		}
		if (algGroupPreprocessor == null) {
			System.exit(-1);
		}
	}
	
	private void parseFile() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			String line = "";
			while ((line = reader.readLine()) != null) {
				handleLine(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	private String createPath(String fileName) {
		return "configs" + File.separator + fileName;
	}
	
	private void handleLine(String line) {
		if (line.trim().equals("")) return;
		String[] split = line.split(":");
		String key = split[0].trim();
		String data = split[1].trim();
		switch (key) {
		case BENCHMARKTYPE_ARG:
			type = parseBenchmarkType(data);
			break;
		case SOLVERS_ARG:
			solvers = parseSolvers(data);
			break;
		case ALGORITHMS_ARG:
			algorithms = parseAlgorithms(data);
			break;
		case FILES_ARG:
			files = parseFiles(data);
			break;
		case TIMELIMIT_ARG:
			timeLimit = parseIntegerValue(key, data);
			break;
		case MEMORYLIMIT_ARG:
			memoryLimit = parseIntegerValue(key, data);
			break;
		case VERIFYRESULTS_ARG:
			verifyResults = parseBooleanValue(data);
			break;
		case SEPARATE_FILES:
			separateFiles = parseBooleanValue(data);
			break;
		case INCREMENT_SIZE:
			incrementSize = parseIntegerValue(key, data);
			break;
		case PROCESSED_INCREMENTS_ARG:
			processedIncrements = parseIntegerValue(key, data);
			break;
		case TIMEUNIT_ARG:
			timeUnit = parseTimeUnit(data);
			break;
		case RANDOMSEED_ARG:
			random = new Random(parseIntegerValue(key, data));
			break;
		case WARMUP_MODEL_ARG:
			warmupModel = data;
			break;
		default:
			break;
		} 
	}
	
	
	private BenchmarkType parseBenchmarkType(String value) {
		if (Arrays.stream(COMPARE_SOLVERS_TYPES).anyMatch(value::equals)) {
			return BenchmarkType.COMPARE_SOLVERS;
		}
		else if (Arrays.stream(COMPARE_ALGORITHMS_TYPES).anyMatch(value::equals)) {
			return BenchmarkType.COMPARE_ALGORITHMS;
		}
		else if (Arrays.stream(PRECISE_ANALYSIS_TYPES).anyMatch(value::equals)) {
			return BenchmarkType.PRECISE_ANALYSIS;
		} 
		else {
			System.out.println("Illegal benchmark type:" + value);
			System.exit(-1);
			return null;
		}
	}
	

	
	private List<IComparableSolver> parseSolvers(String data) {
		SolverProvider provider = new SolverProvider(memoryLimit);
		List<IComparableSolver> solvers = null;
		if (data.startsWith("[")) {
			if (data.endsWith("]")) {
				String solverIds = data.substring(1, data.length() - 1);
				solvers = provider.getSolversByIds(solverIds.split(","));
			} else {
				System.out.println("] missing");
			}
		} else {
			if (data.equals(SELECTION_ALL)) {
				solvers = provider.getAllSolvers();
			} else {
				solvers =  provider.getSolverByType(data);
			}
		}
		if (solvers == null) {
			System.out.println("Illegal selection of solvers: " + data);
			System.exit(-1);
		}

		return solvers;
	}
	
	private List<IComparableAlgorithm> parseAlgorithms(String data) {
		AlgorithmProvider provider = new AlgorithmProvider();
		List<IComparableAlgorithm> algorithms = null;
		if (data.startsWith("[")) {
			if (data.endsWith("]")) {
				String algoIds = data.substring(1, data.length() - 1);
				algorithms = provider.getAlgorithms(algoIds.split(","));
			} else {
				System.out.println("] missing");
			}
		} else {
				algorithms =  provider.getAlgorithmsByGroupId(data);
		}
		if (algorithms == null) {
			System.out.println("Illegal selection of algorithms: " + data);
			System.exit(-1);
		}
		return algorithms;
	}
	
	private List<String> parseFiles(String data) {
		List<String> files = null;
		if (data.startsWith("[")) {
			if (data.endsWith("]")) {
				String filePaths = data.substring(1, data.length() - 1);
				String[] split = filePaths.split(LIST_SEPARATOR);
				files = initializeFileList(Arrays.asList(split));
			} else {
				System.out.println("] missing");
			}
		} else {
			if (data.equals(SELECTION_ALL)) {
				return initializeFileList();
			}
		}
		if (files == null) {
			System.out.println("Illegal selection of files: " + data);
			System.exit(-1);
		}
		if (files.isEmpty()) {
			System.out.println("Directory does not exist or is empty!");
			System.exit(-1);
		}

		return files;
	}
	
	private TimeUnit parseTimeUnit(String value) {
		value = value.trim();
		if (Arrays.stream(NANO_STRINGS).anyMatch(value::equals)) {
			return TimeUnit.NANOSECONDS;
		}
		else if (Arrays.stream(MILLI_STRINGS).anyMatch(value::equals)) {
			return TimeUnit.MILLISECONDS;
		}
		else if (Arrays.stream(SECONDS_STRINGS).anyMatch(value::equals)) {
			return TimeUnit.SECONDS;
		} 
		else if (Arrays.stream(MINUTES_STRINGS).anyMatch(value::equals)) {
			return TimeUnit.MINUTES;
		}
		else if (Arrays.stream(HOURS_STRINGS).anyMatch(value::equals)) {
			return TimeUnit.HOURS;
		} else {
			System.out.println("Illegal time unit: " + value);
			System.exit(-1);
			return null;
		}
	}
	
	
	private List<String> initializeFileList(List<String> includedDirectories) {
		List<String> files = new ArrayList<String>();
		for (String includedDirectory : includedDirectories) {
			files.addAll(FileUtils.getFileNames(FileUtils.getFilesInDirectoryAndSubdirectories(BenchmarkConstants.INPUTMODELS_DIRECTORY + includedDirectory)));
		}
		return files;
	}
	
	
	private List<String> initializeFileList() {
		return FileUtils.getFileNames(FileUtils.getFilesInDirectoryAndSubdirectories(BenchmarkConstants.INPUTMODELS_DIRECTORY));
	}
	
	private int parseIntegerValue(String key, String value) {
		int result = 0;
		try {
			result = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			System.out.println("Illegal argument for " + key + ": " + value);
			System.exit(-1);
		}
		return result;
	}
	
	private boolean parseBooleanValue(String value) {
		return value.equals("true") || value.equals("True");
	}
	
	
	public void updateProcessedCount() {
		String content = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line = "";
			boolean updatedProcessedIncrements = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains(PROCESSED_INCREMENTS_ARG)) {
					int currentCount = Integer.valueOf(line.substring(line.lastIndexOf(":") + 1));
					int newCount = currentCount + 1;
					content += PROCESSED_INCREMENTS_ARG + ":" + newCount;
					processedIncrements = newCount;
					updatedProcessedIncrements = true;
				} else if (line.equals("")) {
				
				}
				else {
					content += line + "\n";					
				}
			}
			if (!updatedProcessedIncrements) {
				content += PROCESSED_INCREMENTS_ARG + ":" + "1"; 
				processedIncrements = 1;
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FileUtils.writeContentToFile(path, content);
	}
	
}
