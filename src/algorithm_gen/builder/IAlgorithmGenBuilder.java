package algorithm_gen.builder;

import java.io.File;

import com.squareup.javapoet.MethodSpec;

public interface IAlgorithmGenBuilder {
	
	public void intializeBuilder();
	
	public MethodSpec getMethod();
	
	public void handleNextLine(String line);
	
	public void preprocessFile(File file);
}
