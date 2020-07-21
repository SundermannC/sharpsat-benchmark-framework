package algorithms.groups.partialconfigurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import algorithms.groups.IGroupPreprocessor;
import algorithms.groups.IPreprocessResult;
import comparablesolver.IComparableSolver;
import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.configuration.Configuration;
import utils.FMUtils;

public class PartialConfigurationsPreprocessor implements IGroupPreprocessor {

	@Override
	public IPreprocessResult getPreprocessData(Random random, IComparableSolver solver, String file, long timeout)
			throws InterruptedException {
		int[] numberOfFeatureSteps = new int[] {2,5,10,50};
		int numberOfConfigurationsPerStep = 50;
		List<Configuration> configurations = new ArrayList<>();
		IFeatureModel model = FMUtils.readFeatureModel(file);
		
		for (int numberOfFeatures : numberOfFeatureSteps) {
			configurations.addAll(FMUtils.createRandomPartialConfigurations(numberOfConfigurationsPerStep, numberOfFeatures, model, random));
		}
		
		return new PartialConfigurationsPreprocessResult(configurations);
	}

}
