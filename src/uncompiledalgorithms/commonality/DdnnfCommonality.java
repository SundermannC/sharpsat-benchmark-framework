package uncompiledalgorithms.commonality;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

import comparablesolver.IComparableSolver;
import ddnnfparsing.SmartDDNNFFormat;
import ddnnfparsing.iterativebottomup.IterativeBottomUpDdnnfFormat;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkConstants;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class DdnnfCommonality {

	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout) {
		
	BinaryResult binaryResult = null;
	SolverResult solverResult = null;
	
	String modelName = FileUtils.getFileNameWithoutExtension(file);
	
	//#RT:ReadModel
	IFeatureModel model = FMUtils.readFeatureModel(file);
	
	//#RT:SaveDimacs
	FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
	
	//#CSOL:"ddnnf";SAVE 
	
	IterativeBottomUpDdnnfFormat format = new IterativeBottomUpDdnnfFormat();
	
	format.readDdnnfFileAndSaveCoreDead(BenchmarkConstants.DDNNF_TEMP_PATH);
	
	//#RT:ComputeCommonalities
	List<BigInteger> commonalities = format.getCommonalities();
	
	
	//#RESULT:"commonalities";commonalities.toString()
	
	//#RETURN
	}
}
