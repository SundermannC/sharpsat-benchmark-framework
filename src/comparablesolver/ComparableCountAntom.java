package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableCountAntom implements IComparableSolver {

	
	private final static String BINARY_NAME = "countAntom";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;

	private final static String ID = "countAntom";
	
	private static final String UNSAT_FLAG = "s UNSATISFIABLE";
	
	private static final int DEFAULT_NUMBER_OF_THREADS = 4;
	
	private int numberOfThreads;
	private int memoryLimit;
	
	
	public ComparableCountAntom(int memoryLimit) {
		this(memoryLimit, DEFAULT_NUMBER_OF_THREADS);
	}
	
	
	public ComparableCountAntom(int memoryLimit, int numberOfThreads) {
		this.memoryLimit = memoryLimit;
		this.numberOfThreads = numberOfThreads;
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException {
		String command = buildCommand(dimacsPath);
		BinaryResult output = runner.runBinary(command, timeout);
		return output;
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws InterruptedException {
		return executeSolver(runner, dimacsPath, timeout);
	}

	@Override
	public SolverResult getResult(String output) {
		if (isUNSAT(output)) {
			return SolverResult.getSolvedResult("0");
		}
		final Pattern pattern = Pattern.compile("model count.*\\d*");
		final Matcher matcher = pattern.matcher(output);
		String result = "";
		if (matcher.find()) {
			result = matcher.group();
		} else {
			return SolverResult.getUnexpectedErrorResult();
		}
		final String[] split = result.split(" ");
		return SolverResult.getSolvedResult(split[split.length - 1]);
	}

	
	private boolean isUNSAT(String output) {
		return output.contains(UNSAT_FLAG);
	}

	@Override
	public String getComputedMetaData(String output) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String buildCommand(String dimacsPath) {
		final String command = BinaryRunner.MONITORING_PREFIX +  BINARY_PATH + " --memSize=" + memoryLimit + " --noThreads=" + numberOfThreads + " " + dimacsPath;
		return command;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}
	
	@Override
	public String getSolverType() {
		return SolverTypes.DPLL;
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}


}
