package utils;

import java.io.File;

public class BenchmarkConstants {
	public static final int MEMORY_LIMIT_REACHED_INDICATOR = -1000;
	
	public static final int UNEXPECTED_ERROR_INDICATOR = -222;
	
	public static final int WRONG_RESULT_INDICATOR = -77;
	
	
	
	public static final String INPUTMODELS_DIRECTORY = "models" + File.separator;
	
	public static final String RESULTS_DIRECTORY = "results" + File.separator;
	
	public static final String COMPARE_SOLVER_RESULTS_DIRECTORY = RESULTS_DIRECTORY + "compareSolver" + File.separator;
	
	public static final String COMPARE_ALGORITHM_RESULTS_DIRECTORY = RESULTS_DIRECTORY + "compareAlgorithm" + File.separator;
	
	public static final String PRECISE_ANALYSIS_RESULTS_DIRECTORY = RESULTS_DIRECTORY + "preciseAnalysis" + File.separator;
	
	public static final String VERIFY_DIRECTORY = "verify_data" + File.separator;
	
	public static final String DDNNF_TEMP_PATH = "temp.dimacs.nnf";

}
