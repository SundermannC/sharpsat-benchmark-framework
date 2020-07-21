package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparablePicosat implements IComparableSolver {


	private final static String ID = "picosat";

	private final static String BINARY_NAME = "picosat";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;
	
	private int memoryLimit;
	
	private final static String COUNTMODE_FLAGS = "--all -n";
	
	public ComparablePicosat(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException {
		String[] command = buildCommand(dimacsPath);
		BinaryResult output = runner.runBinary(command);
		return output;
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws InterruptedException {
		return executeSolver(runner, dimacsPath, timeout);
	}

	@Override
	public SolverResult getResult(String output) {
		final Pattern pattern = Pattern.compile("s\\s+SOLUTIONS\\s+\\d*");
		final Matcher matcher = pattern.matcher(output);
		String result = "";
		if (matcher.find()) {
			result = matcher.group();
		} else {
			return SolverResult.getUnexpectedErrorResult();
		}
		final String[] split = result.split("\\s+");
		return SolverResult.getSolvedResult(split[split.length - 1]);
	}

	@Override
	public String getComputedMetaData(String output) {
		// TODO Auto-generated method stub
		return null;
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

	
	
	
	private String[] buildCommand(String dimacsPath) {
		String solverCall = BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " + COUNTMODE_FLAGS + " " + dimacsPath;
		return new String[] {BinaryRunner.SCRIPT_PREFIX, "-c", BinaryRunner.getUlimitString(memoryLimit) + solverCall};
	}

}
