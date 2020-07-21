package uncompiledalgorithms.partialconfigurations;

import java.util.List;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.partialconfigurations.PartialConfigurationsPreprocessResult;
import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class NaivePartialConfigurations {

	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) {
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		List<Configuration> configurations = ((PartialConfigurationsPreprocessResult)preprocessResult).configurations;
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:SaveDimacs
		FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
		
		//#CSOL:modelName
		
		//#RESULT:modelName;solverResult.result.toString()
				
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		int currentIndex = 1;
		for (Configuration config : configurations) {
			//#RTB:ChangeFormula
			CNF temp = cnf.clone();
			for (IFeature feat : config.getSelectedFeatures()) {
				String featName = feat.getName();
				int varIndex = temp.getVariables().getVariable(featName);
				temp.addClause(new LiteralSet(varIndex));
			}
			for (IFeature feat : config.getUnSelectedFeatures()) {
				String featName = feat.getName();
				int varIndex = temp.getVariables().getVariable(featName);
				temp.addClause(new LiteralSet(-varIndex));
			}
			//#RTE:ChangeFormula
			
			//#RT:SaveDimacs
			DIMACSUtils.createTemporaryDimacs(temp);
			
			String name = "config_" + currentIndex;
			
			//#CSOL:name
			
			//#RESULT:name;solverResult.result.toString()
			currentIndex++;
		}
		
		//#RETURN
	}
	
}
