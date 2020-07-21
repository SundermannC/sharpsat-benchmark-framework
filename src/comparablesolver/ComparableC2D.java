package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableC2D implements IComparableSolver {


	
	private final static String BINARY_NAME = "c2d";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME;
	
	private final static String MEMORY_FLAG = "-cache_size";
	
	private final static String ID = "c2d";
	
	private final static String SMOOTH_FLAG = "-smooth_all";
	
	private final static String OPTIMIZED_FOR_COUNTING_FLAGS = "-count -in_memory";
	
	private final static String FILE_FLAG = "-in";
	
	private int memoryLimit;
	
	public ComparableC2D(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}
	
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException {
		return executeSolver(runner, dimacsPath, timeout, false);
	}
	
	@Override
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws InterruptedException {
		String command = buildCommand(dimacsPath, saveResultingFormat);
		BinaryResult output = runner.runBinary(command, timeout);
		return output;
	}

	@Override
	public SolverResult getResult(String output) {
		final Pattern pattern = Pattern.compile("Counting\\.\\.\\.\\d*");
		final Matcher matcher = pattern.matcher(output);
		String result = "";
		if (matcher.find()) {
			result = matcher.group();
		} else {
			if (output.contains("Compile Time:")) {
				return SolverResult.getNoResult();
			} else {
				return SolverResult.getUnexpectedErrorResult();
			}
		}
		final String[] split = result.split("\\.\\.\\.");
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
	
	private String buildCommand(String dimacsPath, boolean saveDdnnf) {
		String saveDdnnfPart = saveDdnnf ? "" : " " + OPTIMIZED_FOR_COUNTING_FLAGS;
		return BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " +  FILE_FLAG + " " + dimacsPath + saveDdnnfPart + " " + SMOOTH_FLAG
				+ " " + MEMORY_FLAG + " " + memoryLimit;
	}
	

	@Override
	public String getSolverType() {
		return SolverTypes.DDNNF;
	}


	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}

}
