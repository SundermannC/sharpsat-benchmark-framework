package algorithms.basics;

import java.util.ArrayList;
import java.util.List;

import algorithms.groups.commonality.DdnnfCommonality;
import algorithms.groups.commonality.DdnnfPropCommonality;
import algorithms.groups.commonality.NaiveCommonality;
import algorithms.groups.commonality.PropogateAnalysisResultsCommonality;
import algorithms.groups.commonality.PropogateTreePropertiesCommonality;
import algorithms.groups.count.NaiveModelCount;
import algorithms.groups.partialconfigurations.DdnnfPartialConfigurations;
import algorithms.groups.partialconfigurations.NaivePartialConfigurations;
import algorithms.groups.random.RandomOrderDdnnf;
import algorithms.groups.random.RandomOrderModelCount;
import algorithms.groups.urs.BottomupDdnnfUniformRandomSampling;
import algorithms.groups.urs.DdnnfUniformRandomSampling;
import algorithms.groups.urs.KUSUniformRandomSampling;
import algorithms.groups.urs.NaiveUniformRandomSampling;
import algorithms.groups.urs.PropogateTreePropertiesUniformRandomSampling;

public class AlgorithmProvider {
	
	List<IComparableAlgorithm> algorithmList;
	
	public AlgorithmProvider() {
		algorithmList = new ArrayList<>();
		// Id: count
		algorithmList.add(new NaiveModelCount());
		
		// Id: random
		algorithmList.add(new RandomOrderModelCount());
		algorithmList.add(new RandomOrderDdnnf());
		
		// Id: commonality
		algorithmList.add(new NaiveCommonality());
		algorithmList.add(new PropogateTreePropertiesCommonality());
		algorithmList.add(new PropogateAnalysisResultsCommonality());
		algorithmList.add(new DdnnfCommonality());
		algorithmList.add(new DdnnfPropCommonality());
		
		// Id: urs
		algorithmList.add(new NaiveUniformRandomSampling());
		algorithmList.add(new PropogateTreePropertiesUniformRandomSampling());
		algorithmList.add(new BottomupDdnnfUniformRandomSampling());
		algorithmList.add(new DdnnfUniformRandomSampling());
		algorithmList.add(new KUSUniformRandomSampling());
		
		//Id: partialconfigurations
		algorithmList.add(new NaivePartialConfigurations());
		algorithmList.add(new DdnnfPartialConfigurations());
	}
	
	
	/**
	 * 
	 * @param strings
	 * @return
	 */
	public List<IComparableAlgorithm> getAlgorithms(String[] strings) {
		List<IComparableAlgorithm> algorithms = new ArrayList<IComparableAlgorithm>();
		for (String algoId : strings) {
			algorithms.add(getAlgorithm(algoId));
		}
		return algorithms;
	}
	
	
	/**
	 * 
	 * @param algoIdentifier
	 * @return
	 */
	public IComparableAlgorithm getAlgorithm(String algoIdentifier) {
		for (IComparableAlgorithm alg : algorithmList) {
			if (alg.getAlgorithmId().equals(algoIdentifier)) {
				return alg;
			}
		}
		System.out.println("Algorithm with id " + algoIdentifier + " was not found.");
		return null;
	}
	
	/**
	 * Returns all added algorithms with the specified groupID
	 * The groupIDs should be defined in AlgorithmGroups
	 * @param groupId 
	 * @return list of the algorithms in the group
	 */
	public List<IComparableAlgorithm> getAlgorithmsByGroupId(String groupId) {
		List<IComparableAlgorithm> algGroup = new ArrayList<>();
		for (IComparableAlgorithm alg : algorithmList) {
			if (groupId.equals(alg.getAlgorithmGroupId())) {
				algGroup.add(alg);
			}
		}
		
		if (algGroup.size() == 0) {
			System.out.println("No algorithm found for the group id " + groupId);
		}
		
		return algGroup;
	}
	
}
