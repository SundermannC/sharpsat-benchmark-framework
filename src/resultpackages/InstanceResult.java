package resultpackages;

import java.math.BigInteger;

import utils.BenchmarkUtils.Status;

public class InstanceResult {
	public long runtime;
	public BigInteger result;
	public Status status;
	public String maxMemory;
	
	public String addititionalInfo;
	
	public InstanceResult() {}
	
	public InstanceResult(long runtime, BigInteger result, Status status) {
		this.runtime = runtime;
		this.result = result;
		this.status = status;
	}
	
	
	public void addAdditionalInfo(String additionalInfo) {
		this.addititionalInfo = additionalInfo;
	}
	
	public static InstanceResult mergeBinaryAndSolverResult(SolverResult solverResult, BinaryResult binaryResult, long runtime, long timeout) {
		InstanceResult resultPackage = new InstanceResult();
		resultPackage.maxMemory = binaryResult.getMaxMemoryString();
		if (solverResult.status.equals(Status.SOLVED)) {
			resultPackage.status = Status.SOLVED;
			resultPackage.runtime = runtime;
			resultPackage.result = solverResult.result;
		} else {
			resultPackage.result = new BigInteger("-1");
			if (binaryResult.status.equals(Status.TIMEOUT)) {
				resultPackage.status = Status.TIMEOUT;
				resultPackage.runtime = timeout;
			} else if (binaryResult.status.equals(Status.MEMORY_LIMIT_REACHED) 
					|| solverResult.status.equals(Status.MEMORY_LIMIT_REACHED)) {
				resultPackage.status = Status.MEMORY_LIMIT_REACHED;
				resultPackage.runtime = runtime;
			} else if (solverResult.status.equals(Status.UNEXPECTED)) {
				resultPackage.status = Status.UNEXPECTED;
				resultPackage.runtime = runtime;
			}
		}
		return resultPackage;	
	}
	
	
}
