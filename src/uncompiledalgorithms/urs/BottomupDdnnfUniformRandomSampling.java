package uncompiledalgorithms.urs;

import java.math.BigInteger;
import java.util.List;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.urs.URSPreprocessorResult;
import comparablesolver.IComparableSolver;
import ddnnfparsing.bottomup.BottomupDdnnfFormat;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeature;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class BottomupDdnnfUniformRandomSampling {

	
	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) {
		URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
		BinaryResult binaryResult;
		SolverResult solverResult;
		String modelName = FileUtils.getFileNameWithoutExtension(file);

		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		DIMACSUtils.createTemporaryDimacs(cnf);
		
		
		//#CSOL:modelName;SAVE
		
		BottomupDdnnfFormat format = new BottomupDdnnfFormat();
		format.readDdnnfFile(BenchmarkConstants.DDNNF_TEMP_PATH);
		
		for (BigInteger randomNumber : ursPreprocessResult.randomNumbers) {
			BigInteger originalRandomNumber = randomNumber;
			format.ursInit(randomNumber);
			for (IFeature feat : model.getFeatures()) {
				format.ursHandleNextVariable(cnf.getVariables().getVariable(feat.getName()));
			}
			
			List<Integer> config = format.ursFinish();
			String resultString = "";
			for (Integer included : config) {
				resultString += included + ",";
			}
			
			//#RESULT:modelName + originalRandomNumber.toString();resultString
		}


		
		//#RETURN
	}
	
	
}
