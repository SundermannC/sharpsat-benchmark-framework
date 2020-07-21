package algorithms.groups;

import java.util.Random;

import comparablesolver.IComparableSolver;

public interface IGroupPreprocessor {
	
	
	public IPreprocessResult getPreprocessData(Random random, IComparableSolver solver,String file, long timeout) throws InterruptedException;
	
}
