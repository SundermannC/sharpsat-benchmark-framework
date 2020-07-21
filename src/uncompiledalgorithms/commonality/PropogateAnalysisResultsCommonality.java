package uncompiledalgorithms.commonality;

import java.util.HashMap;
import java.util.Set;

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

public class PropogateAnalysisResultsCommonality {

	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout) {
		
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		String overallModelCount = "";
		
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:Core
		Set<IFeature> coreFeatures = FMUtils.getCoreFeatures(model);
		
		//#RT:Dead
		Set<IFeature> deadFeatures = FMUtils.getDeadFeatures(model);
		
		//#RT:FalseOptional
		Set<IFeature> falseOptionalFeatures = FMUtils.getFalseOptionalFeatures(model);
		
		//#RT:CreateCNF
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		
		DIMACSUtils.createTemporaryDimacs(cnf);
		
		//#CSOL:modelName
		
		//#RESULT:modelName;solverResult.result.toString()
		
		overallModelCount = solverResult.result.toString();
		
		HashMap<String, String> commonalities = new HashMap<>();
		
		for (IFeature feat : FMUtils.getFeaturesInOrder(model)) {
			
			String featName = feat.getName();
			if (coreFeatures.contains(feat)) {
				commonalities.put(featName, overallModelCount);
			}  else if (deadFeatures.contains(feat)) {
				commonalities.put(featName, "0");
			} else if (feat.getStructure().isMandatory() || falseOptionalFeatures.contains(feat)) {
				commonalities.put(featName, commonalities.get(FMUtils.getParent(feat).getName()));
			} else {
				//#RTB:ChangeFormula
				CNF temp = cnf.clone();
				
				int varIndex = temp.getVariables().getVariable(featName);
				temp.addClause(new LiteralSet(varIndex));
				//#RTE:ChangeFormula
				
				//#RT:SaveDimacs
				DIMACSUtils.createTemporaryDimacs(temp);
				
				
				//#CSOL:currentName
				

				commonalities.put(featName, solverResult.result.toString());
				
			}

		}
		
		//#RESULT:"commonalities";commonalities.toString()
		
		//#RETURN
	}
	
	
}
