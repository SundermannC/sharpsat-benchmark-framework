package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableCNF2EADT implements IComparableSolver {


	private final static String ID = "cnf2eadt";
	
	private final static String BINARY_NAME = "cnf2eadt";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;
	
	private final static String MEMORY_FLAG = "-mem-lim=";
	
	private int memoryLimit;
	
	public ComparableCNF2EADT(int memoryLimit) {
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
		final Pattern pattern = Pattern.compile("(?m)^#Models: [\\d]+$");
		final Matcher matcher = pattern.matcher(output);
		if (matcher.find()) {
			String line = matcher.group();
			String result = line.substring(line.lastIndexOf(":") + 2);
			return SolverResult.getSolvedResult(result);
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
		return SolverTypes.EADT;
	}
	
	private String buildCommand(String dimacsPath) {
		return BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " + MEMORY_FLAG + memoryLimit + " " + dimacsPath;
	}
	

	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}

}
