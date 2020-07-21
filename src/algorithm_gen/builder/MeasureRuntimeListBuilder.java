package algorithm_gen.builder;

import java.io.File;
import java.math.BigInteger;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.MethodSpec.Builder;

import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import utils.BinaryRunner;

public class MeasureRuntimeListBuilder implements IAlgorithmGenBuilder {

	private Builder builder;
	
	private boolean isListMethod = false;
	
	@Override
	public void intializeBuilder() {
		builder = MethodSpec.methodBuilder("measureRuntime")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addException(InterruptedException.class)
				.addParameter(BinaryRunner.class, "runner")
				.addParameter(ParameterizedTypeName.get(List.class, String.class), "files")
				.addParameter(IComparableSolver.class, "solver")
				.addParameter(int.class, "timeout")
				.addParameter(IPreprocessResult.class, "preprocessResult")
				.addStatement("$T<String, String> results = new $T<>()", Map.class, HashMap.class)
				.beginControlFlow("for(String file : files)")
				.addStatement("Map<String, String> interimResult = measureRuntime(runner,file, solver, timeout, preprocessResult)")
				.addStatement("results.putAll(interimResult)")
				.endControlFlow()
				.addStatement("return results")
				.returns(ParameterizedTypeName.get(Map.class, String.class, String.class));
	}

	@Override
	public MethodSpec getMethod() {
		return builder.build();
	}

	@Override
	public void handleNextLine(String line) {
		if (!isListMethod) return; 

	}

	@Override
	public void preprocessFile(File file) {
		isListMethod = false;
	}

}
