package comparablesolver;

import java.util.ArrayList;
import java.util.List;

public class SolverProvider {

	List<IComparableSolver> solverList;
	List<IComparableSolver> approxSolverList;
	
	public SolverProvider(int memoryLimit) {
		solverList = new ArrayList<>();
		approxSolverList = new ArrayList<>();
		solverList.add(new ComparableC2D(memoryLimit));
		solverList.add(new ComparableCachet(memoryLimit));
		solverList.add(new ComparableCNF2OBDD(memoryLimit));
		solverList.add(new ComparableCountAntom(memoryLimit));
		solverList.add(new ComparableD4(memoryLimit));
		solverList.add(new ComparableDsharp(memoryLimit));
		solverList.add(new ComparableMiniC2D(memoryLimit));
		solverList.add(new ComparablePicosat(memoryLimit));
		solverList.add(new ComparableRelsat(memoryLimit));
		solverList.add(new ComparableSharpSAT(memoryLimit));
		solverList.add(new ComparableSharpCDCL(memoryLimit));
		solverList.add(new ComparableCNF2EADT(memoryLimit));
		
		approxSolverList.add(new ComparableApproxCount(memoryLimit));
		approxSolverList.add(new ComparableApproxMC(memoryLimit));
	}

	/**
	 * This does only return exact solvers
	 * @return
	 */
	public List<IComparableSolver> getAllSolvers() {
		return solverList;
	}
	
	public List<IComparableSolver> getSolversByIds(String[] solverIds) {
		List<IComparableSolver> solvers = new ArrayList<>();
		
		for (String solverId : solverIds) {
			IComparableSolver solver = getSolverById(solverId);
			if (solver != null) {
				solvers.add(getSolverById(solverId));
			}

		}
		return solvers;
	}
	
	public IComparableSolver getSolverById(String solverId) {
		for (IComparableSolver solver : solverList) {
			if (solver.getIdentifier().equals(solverId)) {
				return solver;
			}
		}
		for (IComparableSolver solver : approxSolverList) {
			if (solver.getIdentifier().equals(solverId)) {
				return solver;
			}
		}
		System.out.println("No solver available with the id " + solverId +
				"\nDid you add it at comparablesolver/SolverProvider.java?");
		return null;
	}
	
	public List<IComparableSolver> getSolverByType(String type) {
		List<IComparableSolver> solvers = new ArrayList<>();
		for (IComparableSolver solver : solverList) {
			if (solver.getSolverType().equals(type)) {
				solvers.add(solver);
			}
		}
		if (solvers.size() == 0) {
			System.out.println("No solver found for the type " + type);
		}
		return solvers;
	}
	
	
}
