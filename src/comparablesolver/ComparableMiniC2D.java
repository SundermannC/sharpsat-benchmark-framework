package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public class ComparableMiniC2D implements IComparableSolver {

	private final static String BINARY_NAME = "miniC2D";
	
	private final static String BINARY_DIR = "bin";
	
	private final static String BINARY_PATH = "solvers" + File.separator + BINARY_NAME + File.separator 
			+ BINARY_DIR + File.separator + "linux" + File.separator + BINARY_NAME;
		
	private final static String ID = "miniC2D";
	
	private final static String OPTIMIZED_FOR_COUNTING_FLAGS = "--model_counter --in_memory";
	
	private final static String FILE_FLAG = "--cnf";
	
	private int memoryLimit;
	
	public ComparableMiniC2D(int memoryLimit) {
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
		final Pattern pattern = Pattern.compile("Count\\s+\\d+");
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
	
	private String[] buildCommand(String dimacsPath, boolean saveDdnnf) {
		String saveDdnnfPart = saveDdnnf ? "" : " " + OPTIMIZED_FOR_COUNTING_FLAGS;
		String solverCall = BinaryRunner.MONITORING_PREFIX + BINARY_PATH + " " +  FILE_FLAG + " " + dimacsPath + saveDdnnfPart;
		return new String[] {BinaryRunner.SCRIPT_PREFIX, "-c", BinaryRunner.getUlimitString(memoryLimit) + solverCall};
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
