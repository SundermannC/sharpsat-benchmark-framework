package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.BinaryRunner;

public class ComparableD4 implements IComparableSolver {


	private final static String ID = "d4";
	
	private final static String BINARY_NAME = "d4";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;
	
	private int memoryLimit;
	
	
	public ComparableD4(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
	
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException {
		return executeSolver(runner, dimacsPath, timeout, false);
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws InterruptedException {
		String[] command = buildCommand(dimacsPath, saveResultingFormat);
		BinaryResult output = runner.runBinary(command);
		return output;
	}

	@Override
	public SolverResult getResult(String output) {
		final Pattern pattern = Pattern.compile("(?m)^s.*\\d*$");
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
		return SolverTypes.DDNNF;
	}
	
	private String[] buildCommand(String dimacsPath, boolean saveDdnnf) {
		String ddnnfString = saveDdnnf ? " -out=" + BenchmarkConstants.DDNNF_TEMP_PATH : "";
		String solverCall = BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " + dimacsPath + ddnnfString;
		return new String[] {BinaryRunner.SCRIPT_PREFIX, "-c", BinaryRunner.getUlimitString(memoryLimit) + solverCall};
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}


}
