package algorithms.groups;

import java.util.List;

import algorithms.basics.IComparableAlgorithm;
import algorithms.groups.partialconfigurations.PartialConfigurationsPreprocessor;
import algorithms.groups.urs.URSPreprocessor;

public class GroupPreprocessorProvider {
	
	public static final String COMMONALITY = "commonality";
	
	public static final String URS = "urs";
	
	public static final String COUNT = "count";
	
	public static final String PARTIAL_CONFIGURATIONS = "partialconfigurations";
	
	public static final String RANDOM = "random";
	
	public static IGroupPreprocessor getGroupProcessorByGroupId(String id) {
		if (id.equals(COMMONALITY)) {
			return new NullGroupPreprocessor();
		} else if(id.equals(URS)) {
			return new URSPreprocessor();
		} else if(id.equals(COUNT)) {
			return new NullGroupPreprocessor();
		} else if (id.equals(PARTIAL_CONFIGURATIONS)) {
			return new PartialConfigurationsPreprocessor();	
		} else if (id.equals(RANDOM)) {
			return new RandomPreprocessor();
		} else {
			return new NullGroupPreprocessor();
		}
	}
	
	public static IGroupPreprocessor getGroupProcessorByGroupIds(List<IComparableAlgorithm> algorithms) {
		IGroupPreprocessor processor = getGroupProcessorByGroupId(algorithms.get(0).getAlgorithmGroupId());
		for (IComparableAlgorithm alg : algorithms) {
			if (!processor.getClass().equals(getGroupProcessorByGroupId(alg.getAlgorithmGroupId()).getClass())) {
				System.out.println("Incompatible algorithm types for a comparison!");
				return null;
			}
		}
		return processor;
	}
}
