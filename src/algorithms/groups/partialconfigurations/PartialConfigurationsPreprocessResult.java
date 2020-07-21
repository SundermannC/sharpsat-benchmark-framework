package algorithms.groups.partialconfigurations;

import java.util.List;

import algorithms.groups.IPreprocessResult;
import de.ovgu.featureide.fm.core.configuration.Configuration;

public class PartialConfigurationsPreprocessResult implements IPreprocessResult {
	public List<Configuration> configurations;

	public PartialConfigurationsPreprocessResult(List<Configuration> configurations) {
		this.configurations = configurations;
	}
	
}
