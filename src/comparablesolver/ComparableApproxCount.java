package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableApproxCount implements IComparableSolver {
	
	private final static String BINARY_NAME = "approxcount";
	
	private final static String BINARY_PATH = "solvers" + File.separator + "approximate" + File.separator + BINARY_NAME;
	
	private final static String ID = "approxcount";
	
	private int memoryLimit;
	
	public ComparableApproxCount(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat)
			throws InterruptedException {
		return executeSolver(runner, dimacsPath, timeout);
	}

	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout)
			throws InterruptedException {
		String command = buildCommand(dimacsPath, timeout);
		BinaryResult output = runner.runBinary(command, timeout);
		return output;
	}

	@Override
	public SolverResult getResult(String output) {
		final Pattern pattern = Pattern.compile("total estimate:\\s+[0-9]*\\.?[0-9]*[eE]?[+-]?\\d+");
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
		return null;
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public String getSolverType() {
		return SolverTypes.APPROXIMATE;
	}

	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}
	
	private String buildCommand(String dimacsPath, long timeout) {
		long timeoutSeconds = timeout * 60;
		String solverCall = BINARY_PATH + " " + String.valueOf(timeoutSeconds);
		return solverCall;
	}

}
