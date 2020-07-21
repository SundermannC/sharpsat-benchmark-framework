package algorithms.groups;

import java.util.Random;

import comparablesolver.IComparableSolver;

public class RandomPreprocessor implements IGroupPreprocessor {

	@Override
	public IPreprocessResult getPreprocessData(Random random, IComparableSolver solver, String file, long timeout)
			throws InterruptedException {
		
		return new SeededRandom(random);
	}

}
