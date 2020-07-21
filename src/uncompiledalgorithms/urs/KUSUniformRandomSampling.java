package uncompiledalgorithms.urs;

import java.io.File;
import java.math.BigInteger;
import java.util.Set;

import algorithms.groups.IPreprocessResult;
import algorithms.groups.urs.URSPreprocessorResult;
import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.analysis.cnf.CNF;
import de.ovgu.featureide.fm.core.analysis.cnf.formula.FeatureModelFormula;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BinaryRunner;
import utils.DIMACSUtils;
import utils.FMUtils;
import utils.FileUtils;

public class KUSUniformRandomSampling {

	//#ALGO
	public void measureRuntime(String file, BinaryRunner runner, IComparableSolver solver, int timeout, IPreprocessResult preprocessResult) throws InterruptedException {
		URSPreprocessorResult ursPreprocessResult = (URSPreprocessorResult) preprocessResult;
		BinaryResult binaryResult;
		SolverResult solverResult;
		String modelName = FileUtils.getFileNameWithoutExtension(file);

		//#RT:ReadModel
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		FeatureModelFormula formula = new FeatureModelFormula(model);
		CNF cnf = formula.getCNF();
		DIMACSUtils.createTemporaryDimacs(cnf, "solvers/KUS/" + DIMACSUtils.TEMPORARY_DIMACS_PATH);
	    runner.runBinary("solvers/KUS.sh", timeout);
		runner.killProcessesByUserAndName("python3");

		
		//#RETURN
	}
	
	
}
