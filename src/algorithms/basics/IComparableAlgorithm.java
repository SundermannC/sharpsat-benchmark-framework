package algorithms.basics;

import java.util.List;
import java.util.Map;

import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import resultpackages.CompareSolverResultPackage;
import resultpackages.PreciseAnalysisResultPackage;
import utils.BinaryRunner;

public interface IComparableAlgorithm {

	/**
	 * 
	 * @param files
	 * @param solvers
	 * @param timeout
	 */
	public CompareSolverResultPackage compareSolvers(BinaryRunner runner, String file, List<IComparableSolver> solvers, int timeout, IPreprocessResult preprocessResult) throws InterruptedException;
	
	/**
	 * 
	 * @param files
	 * @param solvers
	 * @param timeout
	 */
	public Map<String, String> measureRuntime(BinaryRunner runner, List<String> files, IComparableSolver solver ,int timeout, IPreprocessResult preprocessResult) throws InterruptedException;
	
	/**
	 * 
	 * @param file
	 * @param solver
	 * @param timeout
	 * @return
	 */
	public Map<String, String> measureRuntime(BinaryRunner runner, String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws InterruptedException;
	/**
	 * 
	 * @param files
	 * @param solvers
	 * @param timeout
	 */
	public PreciseAnalysisResultPackage preciseAnalysis(BinaryRunner runner, String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws InterruptedException;
	
	/**
	 * 
	 * @return
	 */
	public String getAlgorithmId();
	
	/**
	 * Used to group the algorithms for a comparison
	 * For example: get all the algorithms that compute the algorithm group by id: "commonality"
	 * Available groups are/can be stored in AlgorithmGroups.java
	 * @return
	 */
	public String getAlgorithmGroupId();

}