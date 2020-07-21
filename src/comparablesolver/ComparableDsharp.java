package comparablesolver;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.BinaryRunner;

public class ComparableDsharp implements IComparableSolver {

	private final static String ID = "dsharp";
	
	private final static String BINARY_NAME = "dsharp";
	
	private final static String BINARY_PATH = "solvers" +  File.separator + BINARY_NAME;
	
	private final static String MEMORY_FLAG = "-cs";
	
	private final static String UNSAT_FLAG = "Theory is unsat.";
	
	private int memoryLimit;
	
	
	
	public ComparableDsharp(int memoryLimit) {
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
		if (isUNSAT(output)) {
			return SolverResult.getSolvedResult("0");
		}
		final Pattern pattern = Pattern.compile("\\#SAT \\(full\\).*\\d*");
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

	private boolean isUNSAT(String output) {
		return output.contains(UNSAT_FLAG);
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
		return SolverTypes.DDNNF;
	}
	
	
	private String buildCommand(String dimacsPath, boolean saveDdnnf) {
		String saveDdnnfString = saveDdnnf ? " -Fnnf " + BenchmarkConstants.DDNNF_TEMP_PATH + " -smoothNNF" : "";
		return BinaryRunner.MONITORING_PREFIX + BINARY_PATH + saveDdnnfString + " " + MEMORY_FLAG + " " + memoryLimit + " " + dimacsPath;
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}


}
