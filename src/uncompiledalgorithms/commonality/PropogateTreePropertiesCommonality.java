package uncompiledalgorithms.commonality;

import java.util.HashSet;
import java.util.Set;

import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class PropogateTreePropertiesCommonality {
	
	
	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout) {
		
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		String overallModelCount = "";
		
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:GetTreeCoreFeatures
		Set<String> treeCoreFeatures = FMUtils.getCoreFeatureNamesByTree(model);
		
		//#RT:SaveDimacs
		FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
		
		//#CSOL:modelName
		
		//#RESULT:modelName;solverResult.result.toString()
		
		overallModelCount = solverResult.result.toString();
		
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		
		for (IFeature feat : model.getFeatures()) {
			
			String featName = feat.getName();
			String currentName = modelName + "_" + featName;
			if (treeCoreFeatures.contains(featName)) {
				
				//#RESULT:featName;overallModelCount
			
			} else {
				//#RTB:ChangeFormula
				CNF temp = cnf.clone();
				
				int varIndex = temp.getVariables().getVariable(featName);
				temp.addClause(new LiteralSet(varIndex));
				//#RTE:ChangeFormula
				
				//#RT:SaveDimacs
				DIMACSUtils.createTemporaryDimacs(temp);
				
				
				//#CSOL:currentName
				

				//#RESULT:currentName;solverResult.result.toString()
				
			}

		}
		
		//#RETURN
	}
	

	
	
}
