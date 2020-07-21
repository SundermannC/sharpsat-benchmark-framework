package uncompiledalgorithms.count;

import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class NaiveModelCount {

	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout) {
		
		//#RTB:Init
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		//#RTE:Init
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		//#RT:SaveDimacs
		FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
		
		//#CSOL:modelName
		
		//#RESULT:modelName;solverResult.result.toString()
		
		
		//#RETURN
	}
	
	
}
