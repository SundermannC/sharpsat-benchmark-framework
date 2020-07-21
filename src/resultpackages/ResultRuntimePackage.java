package resultpackages;

import java.util.Map;

public class ResultRuntimePackage {
	public Map<String, String> result;
	public long runtime;
	
	public ResultRuntimePackage(Map<String, String> result, long runtime) {
		this.result = result;
		this.runtime = runtime;
	}
}
