package ddnnfparsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Reads an nnf file and computes meta data
 * @author chico
 *
 */
public class DdnnfAnalysis {
	
	public int numberOfNodes = 0;
	
	public int numberOfEdges = 0;
	
	public int numberOfFeatures = 0;
	
	public float fileSize = 0;
	
	public DdnnfAnalysis(String path) {
		getMetadata(path);
		File ddnnf = new File(path);
		fileSize = ddnnf.length() / 1024;
	}
	
	public void getMetadata(String path) {
		try {
	        BufferedReader reader;
			reader = new BufferedReader(new FileReader(path));
	        String line;
	        boolean breakout = false;
	        while ((line = reader.readLine()) != null) {
	        	String[] split = line.split(" ");
	        	
	    		if (DDNNFParserUtils.isEntry(split)) {
	    			numberOfNodes = Integer.valueOf(split[1]);
	    			numberOfEdges = Integer.valueOf(split[2]);
	    			numberOfFeatures = Integer.valueOf(split[3]);
	    			breakout = true;
	    		}
	    		if (breakout) break;
	        }
	        reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
}
