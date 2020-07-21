package algorithm_gen.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;

import algorithm_gen.LineChecker;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import resultpackages.CompareSolverResultPackage;
import resultpackages.InstanceResult;
import utils.BenchmarkUtils;
import utils.BinaryRunner;

public class CompareSolversBuilder implements IAlgorithmGenBuilder {

	private final static String RESULTS_COMPARE_SOLVERS = "$T<String, List<InstanceResult>> results = new $T<>()";
		
	private Builder builder;
		
	@Override
	public void intializeBuilder() {
		builder = MethodSpec.methodBuilder("compareSolvers")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addException(InterruptedException.class)
				.addParameter(BinaryRunner.class, "runner")
				.addParameter(String.class, "file")
				.addParameter(ParameterizedTypeName.get(List.class, IComparableSolver.class), "solvers")
				.addParameter(int.class, "timeout")
				.addParameter(IPreprocessResult.class, "preprocessResult")
				.addStatement(RESULTS_COMPARE_SOLVERS, Map.class, HashMap.class)
				.addStatement("$T<$T> resultPackage", List.class, InstanceResult.class)
				.returns(CompareSolverResultPackage.class);
	}

	@Override
	public MethodSpec getMethod() {
		return builder.build();
	}

	@Override
	public void handleNextLine(String line) {
		if (LineChecker.isCommandDirective(line)) {
			if (LineChecker.isSolverPartDirective(line)) {
				addSolverPart(line);
			} else	if (LineChecker.isReturnDirective(line)) {
				builder.addStatement("return new CompareSolverResultPackage(solvers, results, ALGORITHM_ID)");
			}
		} else {
			AlgorithmGenUtils.addRawCodeline(builder, line);
		}

	}
	
	/**
	 * Adds entire solver part to the code if directive //#CSOL:<id>|;SAVE|
	 * Id is saved for the entry
	 * SAVE if a resulting format like bdd or ddnnf should be saved
	 * @param line
	 */
	private void addSolverPart(String line) {
		boolean save = line.contains(";SAVE");
		String id = "";
		if (save) {
			id = line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf(";"));
		} else {
			id = line.substring(line.lastIndexOf(":") + 1, line.length());
		}
		
		
		builder
			.addStatement("resultPackage = new $T<>()", ArrayList.class)
			.beginControlFlow("for ($T solver : solvers)", IComparableSolver.class)
			.addStatement("long startTime = System.nanoTime()")
			.addStatement("binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout" + (save ? ",true)" : ")"))
			.addStatement("long runtime = $T.getDurationNano(startTime, System.nanoTime())", BenchmarkUtils.class)
			.addStatement("runner.killProcessesByUserAndName(solver.getBinaryName())")
			.addStatement("solverResult = solver.getResult(binaryResult.stdout)")
			.addStatement("resultPackage.add(InstanceResult.mergeBinaryAndSolverResult(solverResult, binaryResult, runtime, timeout))")
			.endControlFlow()
			.addStatement("results.put($L, resultPackage)", id);
	}

	@Override
	public void preprocessFile(File file) {}

}
