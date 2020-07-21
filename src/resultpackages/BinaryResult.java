package resultpackages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.BenchmarkUtils.Status;

public class BinaryResult {
	public String stdout;
	public Status status;
	
	public BinaryResult(String stdout, Status status) {
		this.stdout = stdout;
		this.status = status;
	}
	
	public String getMaxMemoryString() {
		Pattern pattern = Pattern.compile("Maximum resident set size \\(kbytes\\): \\d+");
		Matcher matcher = pattern.matcher(stdout);
		String max = "";
		if (matcher.find()) {
			max = matcher.group();
		} else {
			return "";
		}
		max = max.substring(max.lastIndexOf(":") + 1, max.length()).trim();
		
		return max;
	}
	
	
}
