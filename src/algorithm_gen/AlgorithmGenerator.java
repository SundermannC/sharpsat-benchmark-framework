package algorithm_gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import algorithm_gen.builder.IAlgorithmGenBuilder;
import algorithm_gen.builder.CompareSolversBuilder;
import algorithm_gen.builder.MeasureRuntimeBuilder;
import algorithm_gen.builder.MeasureRuntimeListBuilder;
import algorithm_gen.builder.PreciseAnalysisBuilder;
import algorithms.basics.IComparableAlgorithm;
import utils.FileUtils;

public class AlgorithmGenerator {
	
	
	private final static String ALGORITHM_ID_STRING = "ALGORITHM_ID";
	
	private final static String ALGORITHM_GROUPID_STRING = "ALGORITHM_GROUPID";
	
	private File file;
	
	private IAlgorithmGenBuilder compareSolvers;
	
	private IAlgorithmGenBuilder measureRuntime;
	
	private IAlgorithmGenBuilder measureRuntimeList;
	
	private IAlgorithmGenBuilder preciseAnalysis;
		
	private String supportMethodsBlock = "";
	
	
	private boolean skipToAlgo = true;
	
	
	private enum SupportFunction {
		LOOKING_FOR_START, LOOKING_FOR_END, NOT_LOOKING_FOR
	}
	
	private SupportFunction handleSupportFunction = SupportFunction.NOT_LOOKING_FOR;
	
	
	private String algorithmGroupId = null;
	
	
	private List<String> imports = new ArrayList<>();
	
	
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Which file should be used for generation? Relative path from src/uncompiledalgorithms/");
		Scanner in = new Scanner(System.in); 
        String input = in.nextLine(); 
        in.close();
        
        if (input.equals("all")) {
        	List<File> files = FileUtils.getFilesInDirectoryAndSubdirectories("src/uncompiledalgorithms");
        	for (File file: files) {
        		AlgorithmGenerator gen = new AlgorithmGenerator(file.getAbsolutePath());
        		gen.createAlgorithmJavaFile();
        	}
        	System.out.println();
        } else {
        	String sourcePath = "src/uncompiledalgorithms/" + input;
        	AlgorithmGenerator gen = new AlgorithmGenerator(sourcePath);
        	gen.createAlgorithmJavaFile();        	
        }
        
	}
	
	public AlgorithmGenerator(String path) throws FileNotFoundException {
		this.file = new File(path);
		
		
		compareSolvers = new CompareSolversBuilder();
		measureRuntime = new MeasureRuntimeBuilder();
		measureRuntimeList = new MeasureRuntimeListBuilder();
		preciseAnalysis = new PreciseAnalysisBuilder();
		
		preprocessFile();
		
		
		compareSolvers.intializeBuilder();
		measureRuntime.intializeBuilder();
		measureRuntimeList.intializeBuilder();
		preciseAnalysis.intializeBuilder();
	}
	
	private void preprocessFile() throws FileNotFoundException {
		Scanner reader = new Scanner(file);
		String line = "";
		boolean lookingForSupportFunctions = false;
		boolean parsingSupportFunctions = false;
		
		while(reader.hasNextLine()) {
			line = reader.nextLine();
			if (lookingForSupportFunctions) {
				if (LineChecker.isSupportFunctionStartDirective(line)) {
					parsingSupportFunctions = true;
				} else if(LineChecker.isSupportFunctionEndDirective(line)) {
					break;
				} else if (parsingSupportFunctions) {
					supportMethodsBlock += line + "\n";
				}
			} else {
				tryToGetPackage(line);
				handleImport(line);
				if (LineChecker.isAlgoDirective(line)) lookingForSupportFunctions = true;
			}
		}
		reader.close();
		
		compareSolvers.preprocessFile(file);
		measureRuntime.preprocessFile(file);
		measureRuntimeList.preprocessFile(file);
		preciseAnalysis.preprocessFile(file);
	}

	
	public void createAlgorithmJavaFile() throws FileNotFoundException {
		Scanner reader = new Scanner(file);
		String line = "";
		
		while(reader.hasNextLine()) {
			line += reader.nextLine();
			if (handleLine(line)) {
				if (LineChecker.isReturnDirective(line)) break;
				line = "";
			}
		}
		reader.close();
		
		TypeSpec outputClass = createClass(FileUtils.getFileNameWithoutExtension(file.getPath()), algorithmGroupId);
		
		JavaFile javaFile = JavaFile.builder("algorithms.groups." + algorithmGroupId, outputClass)
			    .build();
		String importsInjected = injectImports(javaFile);
		String result = injectSupportFunctions(importsInjected);
		String outputPath = "src/algorithms/groups/" + algorithmGroupId + "/" + FileUtils.getFileNameWithoutExtension(file.getPath()) + ".java";
		System.out.println(result);
		FileUtils.writeContentToFile(outputPath, result);
	}
	
	
	private boolean handleLine(String line) {
		if (skipToAlgo) {
			if (LineChecker.isAlgoDirective(line)) {
				skipToAlgo = false;
			}
			return true;
		}
		if (handleSupportFunction == SupportFunction.LOOKING_FOR_START) {
			if (LineChecker.isSupportFunctionStartDirective(line)) {
				handleSupportFunction = SupportFunction.LOOKING_FOR_END;
			}
			return true;
		}
		
		if (handleSupportFunction == SupportFunction.LOOKING_FOR_END) {
			
		}
		
		if (LineChecker.requiresNext(line)) {
			return false;
		}
		if (LineChecker.skipForAll(line)) {
			return true;
		}
		compareSolvers.handleNextLine(line);
		measureRuntime.handleNextLine(line);
		measureRuntimeList.handleNextLine(line);
		preciseAnalysis.handleNextLine(line);
		
		
		return true;
	}



	
	private TypeSpec createClass(String algorithmID, String algorithmGroupID) {
		return TypeSpec.classBuilder(algorithmID)
				.addSuperinterface(IComparableAlgorithm.class)
			    .addField(FieldSpec.builder(String.class, ALGORITHM_ID_STRING)
			            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			            .initializer("$S", algorithmID)
			            .build())
			    .addField(FieldSpec.builder(String.class, ALGORITHM_GROUPID_STRING)
			            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
			            .initializer("$S", algorithmGroupID)
			            .build())
			    .addModifiers(Modifier.PUBLIC)
			    .addMethod(compareSolvers.getMethod())
			    .addMethod(measureRuntime.getMethod())
			    .addMethod(measureRuntimeList.getMethod())
			    .addMethod(preciseAnalysis.getMethod())
			    .addMethod(getAlgorithmIdSpec())
			    .addMethod(getAlgorithmGroupIdSpec())
			    .build();
	}
	
	
	private MethodSpec getAlgorithmIdSpec() {
		return MethodSpec.methodBuilder("getAlgorithmId")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addStatement("return ALGORITHM_ID")
				.returns(String.class).build();

	}
	
	private MethodSpec getAlgorithmGroupIdSpec() {
		return MethodSpec.methodBuilder("getAlgorithmGroupId")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Override.class)
				.addStatement("return ALGORITHM_GROUPID")
				.returns(String.class).build();
	}
	
	
	
	//------------------------------ Support Methods ------------------------------------------
	
	
	private void tryToGetPackage(String line) {
		if (algorithmGroupId != null) {
			return;
		}
		if (line.contains("package")) {
			algorithmGroupId = line.substring(line.lastIndexOf(".") + 1, line.length() - 1);
		}
	}
	
	public String injectImports(JavaFile javaFile) {
		String rawSource = javaFile.toString();
		List<String> result = new ArrayList<>();
		for (String s : rawSource.split("\n", -1)) {
			result.add(s);
			if (s.startsWith("package ")) {
				result.add("");
				for (String i : imports) {
					String importString = "import" + i + ";";
					if (!rawSource.contains(importString)) {
						result.add(importString);
					}
				}
			}
		}
	    return String.join("\n", result);
	}
	
	public String injectSupportFunctions(String rawSource) {
		String before = rawSource.substring(0, rawSource.lastIndexOf("}"));
		String after = "\n}";
		return before + supportMethodsBlock + after;
	}
	
	
	private void handleImport(String line) {
		if (line.trim().startsWith("import")) {
			imports.add(line.substring(line.lastIndexOf(" "), line.length() -1));
		}
	}

	

	
}
