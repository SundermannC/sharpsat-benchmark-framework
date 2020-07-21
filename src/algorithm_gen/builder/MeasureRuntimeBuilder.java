package algorithm_gen.builder;

import java.io.File;
import java.math.BigInteger;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.MethodSpec.Builder;

import algorithm_gen.LineChecker;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import utils.BinaryRunner;

public class MeasureRuntimeBuilder implements IAlgorithmGenBuilder {

	private Builder builder;
	
	
	@Override
	public void intializeBuilder() {
		builder = MethodSpec.methodBuilder("measureRuntime")
		.addModifiers(Modifier.PUBLIC)
		.addAnnotation(Override.class)
		.addException(InterruptedException.class)
		.addParameter(BinaryRunner.class, "runner")
		.addParameter(String.class, "file")
		.addParameter(IComparableSolver.class, "solver")
		.addParameter(int.class, "timeout")
		.addParameter(IPreprocessResult.class, "preprocessResult")
		.addStatement("$T<String, String> results = new $T<>()", Map.class, HashMap.class)
		.returns(ParameterizedTypeName.get(Map.class, String.class, String.class));
		
	}

	@Override
	public MethodSpec getMethod() {
		return builder.build();
	}

	@Override
	public void handleNextLine(String line) {
		if (LineChecker.isCommandDirective(line)) {
			if (LineChecker.isReturnDirective(line)) {
				builder.addStatement("return results");
				return;
			}
			if (LineChecker.isSolverPartDirective(line)) {
				
				addSolverPart(line.contains(";SAVE"));
			}
			if (LineChecker.isResultDirective(line)) {
				addResultPart(line);
			}
			return;
		} else {
			AlgorithmGenUtils.addRawCodeline(builder, line);
		}
		
	}

	@Override
	public void preprocessFile(File file) {}

	private void addSolverPart(boolean save) {
		builder
		.addStatement("binaryResult = solver.executeSolver(runner,DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout" + (save ? ",true)" : ")"))
		.addStatement("solverResult = solver.getResult(binaryResult.stdout)");
	}
	
	private void addResultPart(String line) {
		String[] parameters = line.split(":")[1].split(";");
		if (parameters.length != 2) {
			System.out.println("Wrong number of parameters:" + line + "\n" + "Expected 2 (id, resultString)");
			
		}
		builder.addStatement("results.put($L, $L)", parameters[0], parameters[1]);
	}
	
}
