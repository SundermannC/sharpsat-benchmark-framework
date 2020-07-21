package comparablesolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableSharpCDCL implements IComparableSolver {


	private final static String ID = "sharpCDCL";
	
	private final static String BINARY_NAME = "sharpCDCL";
	
	private final static String BINARY_PATH = "solvers/sharpCDCL/build/dynamic/bin/sharpCDCL";
	
	private final static String MEMORY_FLAG = "-mem-lim=";
	
	private final static String COUNT_FLAG = " -no-pre -countMode=1 ";
	
	private int memoryLimit;
	
	public ComparableSharpCDCL(int memoryLimit) {
		this.memoryLimit = memoryLimit;
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
		final Pattern pattern = Pattern.compile("(?m)^[\\d]+$");
		final Matcher matcher = pattern.matcher(output);
		if (matcher.find()) {
			return SolverResult.getSolvedResult(matcher.group());
		} else {
			return SolverResult.getUnexpectedErrorResult();
		}
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
		return SolverTypes.DPLL;
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}

	
	private String buildCommand(String dimacsPath) {
		return BinaryRunner.MONITORING_PREFIX + BINARY_PATH + COUNT_FLAG + MEMORY_FLAG + memoryLimit + " " + dimacsPath;
	}

}
