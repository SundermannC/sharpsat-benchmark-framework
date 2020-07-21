package algorithm_gen;

public class LineChecker {
	public static boolean isStatement(String line) {
		return line.trim().endsWith(";");
	}
	
	public static boolean isMemoryMeasure(String line) {
		return line.trim().startsWith("//#MEM");
	}
	
	public static boolean isControlflowStart(String line) {
		return line.trim().endsWith("{");
	}
	
	public static boolean isControlFlowEnd(String line) {
		return line.trim().endsWith("}");
	}
	
	public static boolean isCommandDirective(String line) {
		return line.contains("//#");
	}
	
	public static boolean isRuntimeDirective(String line) {
		return line.trim().equals("//#RT");
	}
	
	public static boolean isRuntimeBeginDirective(String line) {
		return line.contains("//#RTB");
	}
	
	public static boolean isRuntimeEndDirective(String line) {
		return line.contains("//#RTE");
	}
	
	public static boolean isSolverPartDirective(String line) {
		return line.contains("//#CSOL:");
	}
	
	public static boolean isResultDirective(String line) {
		return line.contains("//#RESULT");
	}
	
	public static boolean isReturnDirective(String line) {
		return line.contains("//#RETURN");
	}
	
	public static boolean isSupportFunctionStartDirective(String line) {
		return line.contains("//#SUPPORTB");
	}
	
	public static boolean isSupportFunctionEndDirective(String line) {
		return line.contains("//#SUPPORTE");
	}
	
	public static boolean requiresNext(String line) {
		String trimmedLine = line.trim();
		return !(trimmedLine.endsWith(";") || trimmedLine.endsWith("{") || trimmedLine.endsWith("}") || trimmedLine.startsWith("//") || trimmedLine.isEmpty());
	}
	
	public static boolean isAlgoDirective(String line) {
		return line.contains("//#ALGO");
	}
	
	
	public static boolean skipForAll(String line) {
		return line.contains("public void") || line.trim().isEmpty();
	}
}
