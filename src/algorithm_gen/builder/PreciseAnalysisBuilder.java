package algorithm_gen.builder;

import java.io.File;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;

import algorithm_gen.LineChecker;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import resultpackages.PreciseAnalysisResultPackage;
import utils.BinaryRunner;
import utils.DIMACSUtils;

public class PreciseAnalysisBuilder implements IAlgorithmGenBuilder {

	private Builder builder;
	
	private String activeRuntimeTag = null;
	
	@Override
	public void intializeBuilder() {
		builder = MethodSpec.methodBuilder("preciseAnalysis")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S","unused").build())
				.addException(InterruptedException.class)
				.addParameter(BinaryRunner.class, "runner")
				.addParameter(String.class, "file")
				.addParameter(IComparableSolver.class, "solver")
				.addParameter(int.class, "timeout")
				.addParameter(IPreprocessResult.class, "preprocessResult")
				.addStatement("$T results = new $T(file)",PreciseAnalysisResultPackage.class, PreciseAnalysisResultPackage.class)
				.addStatement("$T runtime = $T.getRuntime()", Runtime.class, Runtime.class)
				.addStatement("$T.gc()", System.class)
				.addStatement("long startMemory = runtime.totalMemory() - runtime.freeMemory()")
				.addStatement("long maxMemory = 0")
				.addStatement("long tempMemory")
				.addStatement("$T maxMemorySource = \"\"", String.class)
				.returns(PreciseAnalysisResultPackage.class);

	}

	@Override
	public MethodSpec getMethod() {
		return builder.build();
	}

	@Override
	public void handleNextLine(String line) {
		if (LineChecker.isCommandDirective(line)) {
			if (LineChecker.isSolverPartDirective(line)) {
				addSolverRuntimeStatement(line.contains(";SAVE"));
			} else if (LineChecker.isRuntimeDirective(line)) {
				addRuntimeStatement(line);
			} else if (LineChecker.isRuntimeBeginDirective(line)) {
				addRuntimeBeginStatement(line);
			} else if (LineChecker.isRuntimeEndDirective(line)) {
				addRuntimeEndStatement(line);
			} else if (LineChecker.isReturnDirective(line)) {
				addPreciseAnalysisReturnStatements();
			} else if (LineChecker.isMemoryMeasure(line)) {
				addMemoryMeasureStatement(line);
			}
		} else {
			AlgorithmGenUtils.addRawCodeline(builder, line);
			if (activeRuntimeTag != null) {
				addRuntimeEndStatementByTag(activeRuntimeTag);
			}
		}

	}

	@Override
	public void preprocessFile(File file) {}
	
	// -------------------------- Support methods --------------------------
	
	
	private void addPreciseAnalysisReturnStatements() {
		builder
		.addStatement("results.maxMemory = maxMemory")
		.addStatement("results.maxMemorySource = maxMemorySource")
		.addStatement("return results");
	}
	
	
	private void addRuntimeStatement(String line) {
		addRuntimeBeginStatement(line);
		activeRuntimeTag = getRuntimeParam(line);
	}
	
	private void addSolverRuntimeStatement(boolean save) {
		builder
			.addStatement("results.startClock(\"$L\")", "solver")
			.addStatement("binaryResult = solver.executeSolver(runner, $T.TEMPORARY_DIMACS_PATH, timeout" + (save ? ",true)" : ")"), DIMACSUtils.class)
			.addStatement("results.stopClock(\"$L\")", "solver")
			.addStatement("solverResult = solver.getResult(binaryResult.stdout)");
	}
	
	private void addRuntimeBeginStatement(String line) {
		builder.addStatement("results.startClock(\"$L\")", getRuntimeParam(line));
	}
	
	private void addRuntimeEndStatement(String line) {
		builder.addStatement("results.stopClock(\"$L\")", getRuntimeParam(line));
	}
	
	private void addRuntimeEndStatementByTag(String tag) {
		builder.addStatement("results.stopClock(\"$L\")", tag);
		activeRuntimeTag = null;
	}
	
	
	private void addMemoryMeasureStatement(String line) {
		
		builder.addStatement("$T.gc()", System.class);
		builder.addStatement("tempMemory = startMemory - (runtime.totalMemory() - runtime.freeMemory())");
		builder.beginControlFlow("if (maxMemory < tempMemory)");
		builder.addStatement("maxMemory = tempMemory");
		builder.addStatement("maxMemorySource = \"$L\"", getRuntimeParam(line));
		builder.endControlFlow();
	}
	
	private String getRuntimeParam(String line) {
		String trimmedLine = line.trim();
		String param = trimmedLine.substring(trimmedLine.lastIndexOf(":") + 1, trimmedLine.length());
		return param;
	}



}
