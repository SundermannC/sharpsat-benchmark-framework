package algorithms.groups;

import java.util.Random;

public class SeededRandom implements IPreprocessResult {
	public Random random;
	
	public SeededRandom(Random random) {
		this.random = random;
	}
}
