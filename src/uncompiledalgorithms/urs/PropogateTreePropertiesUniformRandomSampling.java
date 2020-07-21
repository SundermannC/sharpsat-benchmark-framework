package uncompiledalgorithms.urs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.urs.URSPreprocessorResult;
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

public class PropogateTreePropertiesUniformRandomSampling {
	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) {
		URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;

		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		
		String modelName = FileUtils.getFileNameWithoutExtension(file);

		
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:GetTreeCoreFeatures
		Set<IFeature> coreFeatures = FMUtils.getCoreFeatures(model);
		
		//#RT:Dead
		Set<IFeature> deadFeatures = FMUtils.getDeadFeatures(model);
		
		//#RT:FalseOptional
		Set<IFeature> falseOptionalFeatures = FMUtils.getFalseOptionalFeatures(model);
		
		
		//#RT:SaveDimacs
		FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
		
		//#CSOL:modelName
		
		for (BigInteger randomNumber : ursPreprocessResult.randomNumbers) {
			BigInteger originalRandomNumber = randomNumber;
			
			FeatureModelFormula formula = new FeatureModelFormula(model);
			CNF cnf = formula.getCNF();
			List<String> includedFeatures = new ArrayList<>();
			for (IFeature feat : FMUtils.getFeaturesInOrder(model)) {

				String featName = feat.getName();
				int varIndex = cnf.getVariables().getVariable(featName);
				if (coreFeatures.contains(feat)) {
					includedFeatures.add(featName);
					cnf.addClause(new LiteralSet(varIndex));
				} else if (deadFeatures.contains(feat)) {
					cnf.addClause(new LiteralSet(-varIndex));
				} else if (!includedFeatures.contains(feat.getStructure().getParent().getFeature().getName())) {
					cnf.addClause(new LiteralSet(-varIndex));
				} else if (feat.getStructure().isMandatory() || falseOptionalFeatures.contains(feat)) {
					includedFeatures.add(featName);
					cnf.addClause(new LiteralSet(varIndex));
				} else {
					//#RTB:ChangeFormula
					CNF temp = cnf.clone();
					temp.addClause(new LiteralSet(-varIndex));
					//#RTE:ChangeFormula
					
					//#RT:SaveDimacs
					DIMACSUtils.createTemporaryDimacs(temp);
					
					//#CSOL:featName
					
					//#RTB:ProcessCount
					if (solverResult.result.compareTo(randomNumber) >= 0) {
						cnf.addClause(new LiteralSet(-varIndex));
					} else {
						includedFeatures.add(featName);
						cnf.addClause(new LiteralSet(varIndex));
						randomNumber = randomNumber.subtract(solverResult.result);
					}
					//#RTE:ProcessCount
				}
				
			}
			String resultString = String.join(",", includedFeatures);
	 		
			//#RESULT:modelName + originalRandomNumber.toString();resultString
		}
		
		//#RETURN
	}
}
