package fmanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.channels.InterruptedByTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comparablesolver.ComparableCountAntom;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IConstraint;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkUtils.Status;
import utils.BinaryRunner;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class FeatureModelStructureAnalysis {

	public static void main(String[] args) {
		FMUtils.installLibraries();
		if (args.length < 1) {
			System.out.println("Mandatory argument([0]): Input path\n"
					+ "Optional Argument([1]): Output path");
			return;
		}
		String input = args[0];
		String output = "";
		List<String> modelsToSkip = null;
		boolean saveOutput = false;
		String[] statisticsNames = getStatisticsNames();
		if (args.length == 2) {
			output = args[1];
			File outputFile = new File(output);
			if (outputFile.exists()) {
				System.out.println("Outputfile exists. Skipping existing entries");
				modelsToSkip = getAnalyzedModels(output);
			} else {
				FileUtils.writeContentToFile(output, "ModelName;" + FileUtils.mergeIterableToString(Arrays.asList(statisticsNames),  ";") + "\n");					

			}
			saveOutput = true;
		}
		File inputFile = new File(input);
		if (!inputFile.exists()) {
			System.out.println("File not found:" + input);
			return;
		}
		long lastBackup = System.currentTimeMillis();
		
		
		if (inputFile.isDirectory()) {
			List<File> files = FileUtils.getFilesInDirectoryAndSubdirectories(input);
			List<String[]> statisticBundle = new ArrayList<>(); 
			List<String> modelNames = new ArrayList<>();
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}
				String name = FileUtils.getFileId(file);

				if (modelsToSkip != null) {
					if (modelsToSkip.contains(name)) {
						System.out.println("Skipped " + name);
						continue;
					}
				}
				IFeatureModel tempModel = FMUtils.readFeatureModel(file.getPath());
				String[] tempStatistics = getStatistics(tempModel);
				if (!saveOutput) {
					String printString = name + "\n";
					for (int i = 0; i < tempStatistics.length; i++) {
						printString += statisticsNames[i] + ": " + tempStatistics[i] + "\n";
					}
					System.out.println(printString);
				} else {
					statisticBundle.add(tempStatistics);
					modelNames.add(name);
				}
				if (isTimeForBackup(lastBackup)) {
					
					String csvString = FileUtils.getContentOfFile(output);
					for (int i = 0; i < statisticBundle.size(); i++) {
						csvString += modelNames.get(i) + ";" + FileUtils.mergeIterableToString(Arrays.asList(statisticBundle.get(i)), ";") + "\n";
					}
					FileUtils.writeContentToFileAndCreateDirs(output, csvString, true);
					System.out.println("Last backup at:" + new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(new Date()).toString());
					lastBackup = System.currentTimeMillis();
					statisticBundle = new ArrayList<>();
					modelNames = new ArrayList<>();
				}	
			}

		} else {
			IFeatureModel tempModel = FMUtils.readFeatureModel(inputFile.getPath());
			String[] tempStatistics = getStatistics(tempModel);
			String name = inputFile.getName();
			String printString = name + "\n";
			for (int i = 0; i < tempStatistics.length; i++) {
				printString += statisticsNames[i] + ": " + tempStatistics[i] + "\n";
			}
			System.out.println(printString);
		}

	}
	
//	public static void main(String[] args) {
//		FMUtils.installLibraries();
//		IFeatureModel tempModel = FMUtils.readFeatureModel("models/automotive05/automotive05_20021.xml");
//		Map<String, Set<String>> result = getCrossTreeDependencies(tempModel);
//		Map<String, Integer> subtreeAncCounts = getNumberOfAncestors(tempModel);
//		int numberOfEasySubtrees = 0;
//		for (String key : result.keySet()) {
//			if (result.get(key).size() < 3) {
//				numberOfEasySubtrees++;
//			} else {
//				System.out.println(key + "(" + subtreeAncCounts.get(key) + ")" + ": " + result.get(key));
//			}
//		}
//		System.out.println(numberOfEasySubtrees);
//	}
	
	public static boolean isTimeForBackup(long lastBackup) {
		long timeSinceLastBackup = System.currentTimeMillis() - lastBackup;
		return timeSinceLastBackup > 600000;
	}
	
	public static String[] getStatisticsNames() {
		String[] statisticNames = new String[11];
		statisticNames[0] = "#Features";
		statisticNames[1] = "#Constraints";
		statisticNames[2] = "RatioFeaturesInConstraints";
		statisticNames[3] = "AvgConstraintSize";
		statisticNames[4] = "Avg#Children";
		statisticNames[5] = "TreeDepth";
		statisticNames[6] = "#TopFeatures";
		statisticNames[7] = "#LeafFeatures";
		statisticNames[8] = "CTCDensity";
		statisticNames[9] = "ClauseDensity";
		statisticNames[10] = "#SAT";
		
		return statisticNames;
		
	}
	
	public static String[] getStatistics(IFeatureModel model) {
		String[] statistics = new String[11];
		
		statistics[0] = String.valueOf(getNumberOfFeatures(model));
		statistics[1] = String.valueOf(getNumberOfConstraints(model));
		statistics[2] = String.valueOf(getRatioOfFeaturesIncludedInConstraints(model));
		statistics[3] = String.valueOf(getAverageSizeOfConstraints(model));
		statistics[4] = String.valueOf(getAverageNumberOfChildren(model));
		statistics[5] = String.valueOf(getTreeDepth(model));
		statistics[6] = String.valueOf(getNumberOfTopFeatures(model));
		statistics[7] = String.valueOf(getNumberOfLeafFeatures(model));
		statistics[8] = String.valueOf(getCtcDensity(model));
		statistics[9] = String.valueOf(getClauseDensity(model));
		statistics[10] = getNumberOfValidConfigurations(model);
		
		
		return statistics;
	}
	
	
	
	
	// Statistic functions
	

	public static int getNumberOfFeatures(IFeatureModel model) {
		return model.getNumberOfFeatures();
	}
	
	public static int getNumberOfConstraints(IFeatureModel model) {
		return model.getConstraintCount();
	}
	
	public static double getRatioOfFeaturesIncludedInConstraints(IFeatureModel model) {
		int numberOfFeatures = getNumberOfFeatures(model);
		
		List<IConstraint> constraints = model.getConstraints();
		
		Set<IFeature> appearingFeatures = new HashSet<>();
		for (IConstraint constraint : constraints) {
			appearingFeatures.addAll(constraint.getContainedFeatures());
		}
		
		int numberOfFeaturesAppearingInAConstraint = appearingFeatures.size();
		
		return (float)numberOfFeaturesAppearingInAConstraint / numberOfFeatures;
	}
	
	public static double getAverageSizeOfConstraints(IFeatureModel model) {
		double sizeCount = 0;
		int numberOfConstraints = getNumberOfConstraints(model);
		if (numberOfConstraints == 0) {
			return 0;
		}
		for (IConstraint constraint : model.getConstraints()) {
			sizeCount += constraint.getNode().getLiterals().size();
		}
		return sizeCount / getNumberOfConstraints(model);
	}
	
	public static double getAverageNumberOfChildren(IFeatureModel model) {
		int childrenCount = 0;
		int numberOfNonLeafFeatures = 0;
		for (IFeature feature : model.getFeatures()) {
			int numberOfChildren = feature.getStructure().getChildrenCount();
			if (numberOfChildren != 0) {
				numberOfNonLeafFeatures++;
			}
			childrenCount += (feature.getStructure().getChildrenCount());
		}
		return (double)childrenCount / numberOfNonLeafFeatures;
	}
	
	
	public static int getTreeDepth(IFeatureModel model) {
		return getTreeDepthRecursive(model.getStructure().getRoot());
	}
	
	private static int getTreeDepthRecursive(IFeatureStructure structure) {
		List<Integer> depthOfChildren = new ArrayList<>();
		if (structure.getChildrenCount() == 0) {
			return 1; // decide whether 0 or 1 is correct
		}
		for(IFeatureStructure child : structure.getChildren()) {
			depthOfChildren.add(getTreeDepthRecursive(child));
		}
		return Collections.max(depthOfChildren) + 1;
	}
	
	public static int getNumberOfTopFeatures(IFeatureModel model) {
		return model.getStructure().getRoot().getChildren().size();
	}
	
	public static int getNumberOfLeafFeatures(IFeatureModel model) {
		return  getNumberOfLeafChildren(model.getStructure().getRoot());
	}
	
	private static int getNumberOfLeafChildren(IFeatureStructure structure) {
		int count = 0;
		if (structure.getChildrenCount() ==  0) {
			return 1;
		}
		for(IFeatureStructure child :structure.getChildren()) {
			count += getNumberOfLeafChildren(child);
		}
		return count;
	}
	
	public static double getCtcDensity(IFeatureModel model) {
		return (double) getNumberOfConstraints(model) /getNumberOfFeatures(model);
	}
	
	public static double getClauseDensity(IFeatureModel model) {
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		return (double) cnf.getClauses().size() / cnf.getVariables().size();
	}
	
	public static String getNumberOfValidConfigurations(IFeatureModel model) {
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		DIMACSUtils.createTemporaryDimacs(cnf);
		ComparableCountAntom solver = new ComparableCountAntom(8000);
		BinaryResult result = null;
		try {
			result = solver.executeSolver(new BinaryRunner(1), DIMACSUtils.TEMPORARY_DIMACS_PATH, 1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.status == Status.TIMEOUT) {
			return "-1";
		}
		if (result.status == Status.SOLVED) {
			SolverResult parsedResult = solver.getResult(result.stdout);
			if (parsedResult.status == Status.SOLVED) {
				return parsedResult.result.toString();
			}
		}
		return "-2";
	}
	
	public static List<String> getAnalyzedModels(String path) {
		List<String> models = new ArrayList<>();
		try {
			List<String[]> content = FileUtils.readCsvFile(path, ";");
			for (String[] row : content) {
				models.add(row[0]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return models;
	}
	
	
	public static Map<String, Set<String>> getCrossTreeDependencies(IFeatureModel model) {
		Map<String, String> subtreeMap = getFeatureSubtreeMap(model);
		Map<String, Set<String>> dependencies = new HashMap<>();
		
		for (IConstraint constraint : model.getConstraints()) {
			Set<String> containedSubtrees = getIncludedSubtrees(subtreeMap, constraint);
			for (String subtree : containedSubtrees) {
				if (dependencies.containsKey(subtree)) {
					dependencies.get(subtree).addAll(containedSubtrees);
				} else {
					dependencies.put(subtree, containedSubtrees);
				}
			}
		}
		
		return dependencies;
		
	}
	
	// TODO
	public static Map<String, Map<String, Integer>> getConstraintFeatureCount(Map<String, String> subtreeMap, IFeatureModel model) {
		Map<String, Map<String, Integer>> constraintFeatureCounts = new HashMap<>();
		for (IConstraint constraint : model.getConstraints()) {
			for (IFeature feat : constraint.getContainedFeatures()) {
				String subtree = subtreeMap.get(feat.getName());
				if (constraintFeatureCounts.containsKey(subtree)) {
					if (constraintFeatureCounts.get(subtree).containsKey(feat.getName())) {
						constraintFeatureCounts.get(subtree).get(feat.getName());
					}
				}
			}
		}
		return constraintFeatureCounts;
	}
	
	
	public static Set<String> getIncludedSubtrees(Map<String, String> subtreeMap, IConstraint constraint) {
		Set<String> includedSubtrees = new HashSet<>();
		for (IFeature feat : constraint.getContainedFeatures()) {
			includedSubtrees.add(subtreeMap.get(feat.getName()));
		}
		return includedSubtrees;
	}
	
	public static Map<String,String> getFeatureSubtreeMap(IFeatureModel model) {
		Map<String, String> subtreeMap = new HashMap<String, String>();
		IFeatureStructure root = model.getStructure().getRoot();
		for (IFeatureStructure subtree : root.getChildren()) {
			Set<String> subtreeFeatures = getAncestors(subtree);
			for (String subtreeFeature : subtreeFeatures) {
				subtreeMap.put(subtreeFeature, subtree.getFeature().getName());
			}
		}
		return subtreeMap;
	}
	
	public static Map<String, Integer> getNumberOfAncestors(IFeatureModel model) {
		Map<String, Integer> ancestorCounts = new HashMap<>();
		for (IFeatureStructure subtree : model.getStructure().getRoot().getChildren()) {
			ancestorCounts.put(subtree.getFeature().getName(), getAncestors(subtree).size());
		}
		return ancestorCounts;
	}
	
	public static Set<String> getAncestors(IFeatureStructure struct) {
		Set<String> ancestors = new HashSet<>();
		ancestors.add(struct.getFeature().getName());
		for (IFeatureStructure child : struct.getChildren()) {
			ancestors.addAll(getAncestors(child));
		}
		return ancestors;
	}
	
	
	
}
