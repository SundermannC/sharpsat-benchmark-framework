package ddnnfparsing;

import java.math.BigInteger;
import java.util.HashMap;

public interface IDDNNFFormat {
	
	public void handleLine(String line);
	
	public void finish();
	
	public long countNumberOfSolutions();
	
	public HashMap<Integer,BigInteger> computeCommonalities();
	
}
