package comparablesolver;

import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;

public interface IComparableSolver {
	
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout, boolean saveResultingFormat) throws InterruptedException;
	
	public BinaryResult executeSolver(BinaryRunner runner, String dimacsPath, long timeout) throws InterruptedException;
	
	public SolverResult getResult(String output);
	
	public String getComputedMetaData(String output);
	
	public String getIdentifier();
	
	public String getSolverType();
	
	public String getBinaryName();
	
}
