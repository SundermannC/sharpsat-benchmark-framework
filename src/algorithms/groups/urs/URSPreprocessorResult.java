package algorithms.groups.urs;

import java.math.BigInteger;
import java.util.List;

import algorithms.groups.IPreprocessResult;

public class URSPreprocessorResult implements IPreprocessResult {

	public List<BigInteger> randomNumbers;

	public URSPreprocessorResult(List<BigInteger> randomNumbers) {
		this.randomNumbers = randomNumbers;
	}
	
	

}
