package uncompiledalgorithms.partialconfigurations;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.partialconfigurations.PartialConfigurationsPreprocessResult;
import comparablesolver.IComparableSolver;
import ddnnfparsing.DDNNFPropFormat;
import ddnnfparsing.iterativebottomup.IterativeBottomUpDdnnfFormat;
import ddnnfparsing.optimized.OptimizedDdnnfFormat;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class DdnnfPartialConfigurations {

	
	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) {
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		List<Configuration> configurations = ((PartialConfigurationsPreprocessResult)preprocessResult).configurations;
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
				
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		
		DIMACSUtils.createTemporaryDimacs(cnf);
		
		//#CSOL:modelName;SAVE
		
		IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
		//#RT:readddnnf
		format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
		//#MEM:ddnnf
		int currentIndex = 1;
		for (Configuration config : configurations) {
			Set<Integer> included = new HashSet<>();
			Set<Integer> excluded = new HashSet<>();
			//#RTB:ParseConfig
			for (IFeature feat : config.getSelectedFeatures()) {
				String featName = feat.getName();
				int varIndex = cnf.getVariables().getVariable(featName);
				included.add(varIndex);
			}
			for (IFeature feat : config.getUnSelectedFeatures()) {
				String featName = feat.getName();
				int varIndex = cnf.getVariables().getVariable(featName);
				excluded.add(varIndex);
			}
			//#RTE:ParseConfig
			
			String name = "config_" + currentIndex;
			
			BigInteger result = format.getPartialConfigurationCount(included, excluded);
			
			//#RESULT:name;result.toString()
			currentIndex++;
		}
		
		//#RETURN
	}
	
}
