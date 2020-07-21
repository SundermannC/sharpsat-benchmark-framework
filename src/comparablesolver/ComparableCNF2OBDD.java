package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableCNF2OBDD implements IComparableSolver {

	private final static String ID = "CNF2OBDD";
	
	private final static String BINARY_NAME = "bdd_minisat_all_static";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;
	
	
	private int memoryLimit;
	
	public ComparableCNF2OBDD(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
	
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException {
		String[] command = buildCommand(dimacsPath);
		BinaryResult output = runner.runBinary(command);
		return output;
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws  InterruptedException {
		return executeSolver(runner, dimacsPath, timeout);
	}

	@Override
	public SolverResult getResult(String output) {
		if (reachedMemoryLimit(output)) {
			return SolverResult.getMemoryLimitResult();
		}
		final Pattern pattern = Pattern.compile("SAT \\(full\\)\\s+:\\s+\\d*");
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
	
	public boolean reachedMemoryLimit(String output) {
		return output.contains("memory allocation failed") || output.contains("Segmentation fault");
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
		return SolverTypes.BDD;
	}
	
	private String[] buildCommand(String dimacsPath) {
		String solverCall = BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " + dimacsPath;
		return new String[] {BinaryRunner.SCRIPT_PREFIX, "-c", BinaryRunner.getUlimitString(memoryLimit) + solverCall};
	}

	

	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}


}
