package uncompiledalgorithms.random;

import java.util.Random;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.SeededRandom;
import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.LiteralSet;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class RandomOrderDdnnf {
	//#ALGO
	public void measureRuntime(String file, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) {
		
		BinaryResult binaryResult = null;
		SolverResult solverResult = null;
		
		
		
		String modelName = FileUtils.getFileNameWithoutExtension(file);
		
		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		//#MEM:ReadModel
		
		Random random = ((SeededRandom)preprocessResult).random;
	    
		FeatureModelFormula formula = new FeatureModelFormula(model);
	    CNF cnf = formula.getCNF();
		
	    int cnfClauses = cnf.getClauses().size();
	    int numberOfLiterals = 0;
	    for (LiteralSet clause : cnf.getClauses()) {
	    	numberOfLiterals += clause.getLiterals().length;
	    }
	    
		for (int i = 1; i <= 20; i++) {
			//#RT:SaveDimacs

		    DIMACSUtils.createTemporaryRandomizedDimacs(cnf, random);
			
			//#CSOL:modelName;SAVE
			
			//#RESULT:modelName + i;solverResult.result.toString()
			
		}

		
		//#RETURN
	}
	
}
