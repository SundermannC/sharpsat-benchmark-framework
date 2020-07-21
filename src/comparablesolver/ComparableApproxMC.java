package comparablesolver;

import java.io.File;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkUtils.Status;
import utils.BinaryRunner;
import utils.FileUtils;

public class ComparableApproxMC implements IComparableSolver {

	private final static String BINARY_NAME = "approxmc";
	
	private final static String BINARY_PATH = "solvers" + File.separator + "approximate" + File.separator + BINARY_NAME;
	
	private final static String IND_SET_BINARY = "solvers" + File.separator + "approximate" + 
	File.separator + "ind_set" + File.separator + "mis" + File.separator + "mis.py";
	
	private final static String ID = "approxmc";
	
	private final static String MUSER_BINARY = "muser2";
	
	private int memoryLimit;
	
	
	public ComparableApproxMC(int memoryLimit) {
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
		
		String indSetCommand = buildIndependentSetCommand(dimacsPath);
		BinaryResult indSet = runner.runBinary(indSetCommand, timeout);
		if (indSet.status == Status.TIMEOUT) {
			runner.killProcessesByUserAndName(MUSER_BINARY);
			return indSet;
		}
		addIndependetSetToDimacs(indSet.stdout, dimacsPath);
		String command = buildCommand(dimacsPath, timeout);
		BinaryResult output = runner.runBinary(command, timeout);
		return output;
	}

	@Override
	public SolverResult getResult(String output) {
		final Pattern pattern = Pattern.compile("Number of solutions is:\\s+[0-9]*\\s+x\\s+[0-9]*\\^[0-9]*");
		final Matcher matcher = pattern.matcher(output);
		String result = "";
		if (matcher.find()) {
			result = matcher.group();
		} else {
			return SolverResult.getUnexpectedErrorResult();
		}
		final String[] split = result.split("\\s+");
		BigInteger factor1 = new BigInteger(split[4]);
		String factor2String = split[6];
		BigInteger base = new BigInteger(factor2String.substring(0, factor2String.indexOf("^")));
		int exponent = Integer.parseInt(factor2String.substring(factor2String.indexOf("^") + 1));
		BigInteger factor2 = base.pow(exponent);		
		
		return new SolverResult(Status.SOLVED, factor1.multiply(factor2));
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
		String solverCall = BINARY_PATH + " -v 0 " + dimacsPath;
		return solverCall;
	}
	
	private String buildIndependentSetCommand(String dimacsPath) {
		return IND_SET_BINARY + " " + dimacsPath;
	}
	
	private void addIndependetSetToDimacs(String stdout, String dimacsPath) {
		String indSet = getIndSetFromStdout(stdout);
		String content = FileUtils.readFile(dimacsPath);
		FileUtils.writeContentToFile(dimacsPath, indSet + "\n" + content);
		
	}
	
	private String getIndSetFromStdout(String stdout) {
		return stdout.substring(stdout.indexOf("c ind")).trim();
	}

}
