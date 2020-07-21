package main;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import comparablesolver.IComparableSolver;
import comparablesolver.SolverProvider;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;
import utils.FileUtils;

public class SolverTest {
	
	private static final String TEST_DIMACS = "test_dimacs" + File.separator + "simpletest.dimacs";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Which solver to test?");
		Scanner in = new Scanner(System.in); 
        String input = in.nextLine(); 

        in.close();
        if (input.equals("all")) {
        	checkAllSolvers();
        } else {
        	checkSingleSolver(input);
        }
	}
	
	private static void checkAllSolvers() throws InterruptedException {
		SolverProvider provider = new SolverProvider(8000);
		List<IComparableSolver> solvers = provider.getAllSolvers();
		List<String> correctNames = new ArrayList<String>();
		List<String> wrongNames = new ArrayList<String>();
		for (IComparableSolver solver : solvers) {
	        BinaryResult result = solver.executeSolver(null, TEST_DIMACS, 10);
	        SolverResult solverResult = solver.getResult(result.stdout);
	        if (solverResult.result.equals(BigInteger.valueOf(28))) {
	        	correctNames.add(solver.getIdentifier());
	        } else {
	        	wrongNames.add(solver.getIdentifier());
	        }
		}
		
		if (!wrongNames.isEmpty()) {
			System.out.println(wrongNames.size() + " solvers failed:");
			System.out.println(FileUtils.mergeIterableToString(wrongNames, "\n"));
		} else {
			System.out.println("Zero errors!");
		}
		System.out.println("The following solvers computed correct results:");
		System.out.println(FileUtils.mergeIterableToString(correctNames, "\n"));

		
	}
	
	private static void checkSingleSolver(String input) throws InterruptedException {
        SolverProvider provider = new SolverProvider(8000);
        IComparableSolver solver = provider.getSolverById(input);
        if (solver == null) {
        	return;
        }
        BinaryResult result = solver.executeSolver(new BinaryRunner(5), TEST_DIMACS, 10);
        System.out.println(result.stdout);
        SolverResult solverResult = solver.getResult(result.stdout);
        if (solverResult.result.equals(BigInteger.valueOf(28))) {
        	System.out.println("Correct result: " + solverResult.result.toString());
        } else {
        	System.out.println("Wrong result: " + solverResult.result.toString());
        }
	}
}
