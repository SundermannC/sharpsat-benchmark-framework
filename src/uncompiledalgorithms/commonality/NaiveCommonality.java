package uncompiledalgorithms.commonality;

import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class NaiveCommonality {

	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout) {
		
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:SaveDimacs
		FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
		
		//#CSOL:modelName
		
		//#RESULT:modelName;solverResult.result.toString()
				
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		
		for (IFeature feat : model.getFeatures()) {
			//#RTB:ChangeFormula
			CNF temp = cnf.clone();
			
			String featName = feat.getName();
			int varIndex = temp.getVariables().getVariable(featName);
			temp.addClause(new LiteralSet(varIndex));
			//#RTE:ChangeFormula
			
			//#RT:SaveDimacs
			DIMACSUtils.createTemporaryDimacs(temp);
			
			
			//#CSOL:featName
			
			//#RESULT:featName;solverResult.result.toString()
		
		}
		
		//#RETURN
	}
	
	
}
