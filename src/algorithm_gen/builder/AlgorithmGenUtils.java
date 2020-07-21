package algorithm_gen.builder;

import com.squareup.javapoet.MethodSpec.Builder;

import algorithm_gen.LineChecker;

public class AlgorithmGenUtils {

	public static void addRawCodeline(Builder builder, String line) {
		String trimmedLine = line.trim();
		if (LineChecker.isStatement(trimmedLine)) {
			builder.addStatement(trimmedLine.substring(0, trimmedLine.length() -1));
		} else if (LineChecker.isControlflowStart(trimmedLine)) {
			builder.beginControlFlow(trimmedLine.substring(0, trimmedLine.length() -1));
		} else if(LineChecker.isControlFlowEnd(trimmedLine)) {
			builder.endControlFlow();
		}
	}
	
}
