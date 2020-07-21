package resultpackages;

import java.math.BigDecimal;
import java.math.BigInteger;

import utils.BenchmarkUtils.Status;

public class SolverResult {
	public Status status;
	
	public BigInteger result;
	
	public SolverResult() {
	}

	public SolverResult(Status status, BigInteger result) {
		this.status = status;
		this.result = result;
	}
	
	
	public static SolverResult getUnexpectedErrorResult() {
		return new SolverResult(Status.UNEXPECTED, new BigInteger("-1"));
	}
	
	public static SolverResult getNoResult() {
		return new SolverResult(Status.SOLVED, new BigInteger("-1"));
	}
	
	public static SolverResult getMemoryLimitResult() {
		return new SolverResult(Status.MEMORY_LIMIT_REACHED, new BigInteger("-1"));
	}
	
	public static SolverResult getSolvedResult(String numberOfSolutionsString) {
		BigInteger result;
		if (isScientificNotation(numberOfSolutionsString)) {
			BigDecimal bd = new BigDecimal(numberOfSolutionsString);
			result = bd.toBigInteger();
		} else {
			result = new BigInteger(numberOfSolutionsString);
		}
		return new SolverResult(Status.SOLVED, result);
	}
	
	private static boolean isScientificNotation(String result) {
		return (result.contains("e") || result.contains("E"));
	}
	
	
}
