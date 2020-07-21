package algorithms.groups.urs;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import algorithms.groups.IGroupPreprocessor;
import algorithms.groups.IPreprocessResult;
import algorithms.groups.TimeoutPreprocess;
import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import resultpackages.BinaryResult;
import resultpackages.SolverResult;
import utils.BenchmarkUtils.Status;
import utils.BinaryRunner;
import utils.DIMACSUtils;
import utils.FMUtils;

public class URSPreprocessor implements IGroupPreprocessor {

	@Override
	public IPreprocessResult getPreprocessData(Random random, IComparableSolver solver, String file, long timeout) throws InterruptedException {
		int numberOfSamples = 10;
		List<BigInteger> randoms = new ArrayList<>();
	    IFeatureModel model = FMUtils.readFeatureModel(file);
        FMUtils.saveFeatureModelAsDIMACS(model, DIMACSUtils.TEMPORARY_DIMACS_PATH);
        BinaryResult binaryResult = solver.executeSolver(new BinaryRunner(timeout),DIMACSUtils.TEMPORARY_DIMACS_PATH, timeout);
        if (binaryResult.status == Status.TIMEOUT) {
        	return new TimeoutPreprocess();
        }
        SolverResult solverResult = solver.getResult(binaryResult.stdout);
	    for (int i = 0; i < numberOfSamples; i++) {
	        BigInteger randomNumber;
		    do  {
		      randomNumber = new BigInteger(solverResult.result.bitLength(), random);
		    } while (randomNumber.compareTo(solverResult.result) >= 0);
		    randoms.add(randomNumber);
	    }
	    
	    return new URSPreprocessorResult(randoms);
	}

}
