package resultpackages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import comparablesolver.IComparableSolver;

public class CompareSolverResultPackage {
	
	public Map<String, List<InstanceResult>> resultPackage;
	
	public List<String> solverNames;
	
	public String algorithmId;
	
	public CompareSolverResultPackage(List<IComparableSolver> solvers, Map<String, List<InstanceResult>> resultPackage, String algorithmId) {
		this.resultPackage = resultPackage;
		this.algorithmId = algorithmId;
		solverNames = new ArrayList<>();
		for (IComparableSolver solver : solvers) {
			solverNames.add(solver.getIdentifier());
		}
	}
	
	public CompareSolverResultPackage() {
		this.resultPackage = new HashMap<>();
		this.algorithmId = "";
		
	}
	
	public void addNewResults(CompareSolverResultPackage newPackage) {
		resultPackage.putAll(newPackage.resultPackage);
		if (algorithmId.equals("")) {
			this.algorithmId = newPackage.algorithmId;
		}
		if (solverNames == null) {
			solverNames = newPackage.solverNames;
		}
	}
}
